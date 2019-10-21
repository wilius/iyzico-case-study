package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.data.service.PaymentService;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.InvalidInstallmentCountException;
import com.iyzico.challenge.integrator.exception.PaymentException;
import com.iyzico.challenge.integrator.properties.IyzicoProperties;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import com.iyzipay.IyzipayResource;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.InstallmentDetail;
import com.iyzipay.model.InstallmentInfo;
import com.iyzipay.model.InstallmentPrice;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.RetrieveInstallmentInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PaymentManager {
    private final Logger log = LoggerFactory.getLogger(PaymentManager.class);
    private final String COUNTRY = "TR";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PaymentService paymentService;
    private final BasketService basketService;
    private final UserService userService;
    private final LockService lockService;
    private final Options options;

    public PaymentManager(PaymentService paymentService,
                          LockService lockService,
                          BasketService basketService,
                          IyzicoProperties properties,
                          UserService userService) {
        this.paymentService = paymentService;

        this.lockService = lockService;
        this.basketService = basketService;
        this.userService = userService;

        Options options = new Options();
        options.setBaseUrl(properties.getBaseUrl());
        options.setApiKey(properties.getApiKey());
        options.setSecretKey(properties.getSecretKey());
        this.options = options;
    }

    public InstallmentDetail getInstallments(User user, String digits) {
        Basket basket = basketService.getUserBasket(user);
        return getInstallments(user, basket, digits);
    }

    private InstallmentDetail getInstallments(User user, Basket basket, String digits) {
        InstallmentInfo result = lockService.executeInBasketLock(user, () -> {
            RetrieveInstallmentInfoRequest request = new RetrieveInstallmentInfoRequest();
            request.setLocale(COUNTRY);
            request.setBinNumber(digits);
            request.setPrice(basket.getTotal());
            request.setCurrency(Currency.TRY.name());
            request.setConversationId(String.valueOf(basket.getId()));

            return InstallmentInfo.retrieve(request, options);
        });

        validate(result);
        if (result.getInstallmentDetails().isEmpty()) {
            throw new PaymentException(String.format("No installment info found for digits %s", digits));
        }
        return result.getInstallmentDetails().get(0);
    }

    public Payment pay(User user, String holderName, String cardNumber, YearMonth expire, String cvc, String ip,
                       int installment) {
        return lockService.executeInBasketLock(user, () -> doPay(user, holderName, cardNumber, expire, cvc, ip, installment));
    }

    private Payment doPay(User user, String holderName, String cardNumber, YearMonth expire, String cvc, String ip, int installmentCount) {
        log.info("Payment started for user {}. ip: {}, installmentCount: {}", user.getId(), ip, installmentCount);

        Basket basket = null;

        UserProfile profile;
        InstallmentPrice installment;
        UserPayment payment;
        boolean decreased = false;
        try {
            log.info("Getting profile with id {}", user.getUserProfileId());
            // due to lazy loading exception
            profile = userService.getProfileById(user.getUserProfileId());

            basket = basketService.getUserBasket(user);

            installment = validateAndGetInstallment(user, basket, cardNumber, installmentCount);

            log.info("Starting to decrease stocks for user {}", user.getId());
            basket = basketService.decreaseStocks(user, basket);
            decreased = true;

            payment = paymentService.startPayment(user, basket);
        } catch (Throwable t) {
            log.warn("Unexpected exception during the initialization of the payment", t);
            if (decreased) {
                try {
                    basketService.rollbackStocks(user, basket);
                } catch (Throwable t2) {
                    // send a notification to manually solve the problem
                    log.warn("Unexpected exception during the rollback of user basket", t2);
                    throw t2;
                }
            }

            throw t;
        }

        Payment response;
        try {
            response = sendPaymentRequest(user, basket, profile, installment, holderName, cardNumber, expire, cvc, ip);
        } catch (Throwable t) {
            log.warn("Unexpected exception during the payment request", t);
            try {
                paymentService.markAsFailure(payment, t.getMessage());
            } catch (Throwable t2) {
                // send a notification to check and manually solve the problem
                log.warn("Unexpected exception while marking payment as failure", t2);
                throw t2;
            }

            throw t;
        }

        try {
            paymentService.markAsSuccess(user, payment, basket, response);
        } catch (Throwable t) {
            // send a notification to manually solve the problem or queue it to retry
            log.warn("Unexpected exception occurred during the payment", t);
        }

        return response;
    }

    private Payment sendPaymentRequest(User user, Basket basket, UserProfile profile, InstallmentPrice installment, String holderName, String cardNumber, YearMonth expire, String cvc, String ip) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLocale(COUNTRY);
        request.setConversationId(String.valueOf(basket.getId()));
        request.setPrice(basket.getTotal());
        request.setCurrency(Currency.TRY.name());
        request.setBasketId(String.valueOf(basket.getId()));
        request.setPaymentChannel(PaymentChannel.WEB.name());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());

        request.setPaidPrice(installment.getTotalPrice());
        request.setInstallment(installment.getInstallmentNumber());

        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(holderName);
        paymentCard.setCardNumber(cardNumber);
        paymentCard.setExpireMonth(String.valueOf(expire.getMonthValue()));
        paymentCard.setExpireYear(String.valueOf(expire.getYear()));
        paymentCard.setCvc(cvc);
        paymentCard.setRegisterCard(0);
        request.setPaymentCard(paymentCard);

        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(user.getId()));
        buyer.setName(profile.getName());
        buyer.setSurname(profile.getSurname());
        buyer.setGsmNumber(profile.getPhoneNumber());
        buyer.setEmail(profile.getEmail());
        buyer.setIdentityNumber(profile.getIdentityNo());
        buyer.setLastLoginDate(formatter.format(user.getLastLoginDate()));
        buyer.setRegistrationDate(formatter.format(profile.getRegistrationDate()));
        buyer.setRegistrationAddress(profile.getAddress());
        buyer.setIp(ip);
        buyer.setCity(profile.getCity());
        buyer.setCountry(profile.getCountry());
        buyer.setZipCode(profile.getZipCode());
        request.setBuyer(buyer);

        Locale countryLocale = new Locale("", profile.getCountry());
        Address address = new Address();
        address.setContactName(String.format("%s %s", profile.getName(), profile.getSurname()));
        address.setCity(profile.getCity());
        address.setCountry(countryLocale.getDisplayCountry());
        address.setAddress(profile.getAddress());
        address.setZipCode(profile.getZipCode());
        request.setShippingAddress(address);

        request.setBillingAddress(address);

        List<BasketItem> basketItems = new ArrayList<>(basket.getProducts().size());
        for (BasketProduct product : basket.getProducts()) {
            BasketItem item = new BasketItem();
            item.setId(String.valueOf(product.getProductId()));
            item.setName(product.getProduct().getName());
            item.setCategory1("No category");
            item.setItemType(BasketItemType.PHYSICAL.name());
            item.setPrice(product.getSubtotal());
            basketItems.add(item);
        }

        request.setBasketItems(basketItems);

        return validate(Payment.create(request, options));
    }

    private InstallmentPrice validateAndGetInstallment(User user, Basket basket, String cardNumber, int installmentCount) {
        InstallmentDetail installmentInfo = getInstallments(user, basket, cardNumber.substring(0, 6));
        Optional<InstallmentPrice> installmentResult = installmentInfo.getInstallmentPrices()
                .stream()
                .filter(x -> x.getInstallmentNumber() == installmentCount)
                .findFirst();

        if (!installmentResult.isPresent()) {
            throw new InvalidInstallmentCountException("Invalid installment");
        }

        return installmentResult.get();

    }

    private <T extends IyzipayResource> T validate(T result) {
        if (!"success".equalsIgnoreCase(result.getStatus())) {
            throw new PaymentException(String.format("%s: %s", result.getErrorCode(), result.getErrorMessage()));
        }

        return result;
    }
}
