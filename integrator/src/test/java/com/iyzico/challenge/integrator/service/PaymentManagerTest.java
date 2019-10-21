package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.data.service.PaymentService;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.InvalidInstallmentCountException;
import com.iyzico.challenge.integrator.exception.PaymentException;
import com.iyzico.challenge.integrator.properties.IyzicoProperties;
import com.iyzico.challenge.integrator.service.hazelcast.BLock;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.InstallmentDetail;
import com.iyzipay.model.InstallmentInfo;
import com.iyzipay.model.InstallmentPrice;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.request.CreatePaymentRequest;
import com.iyzipay.request.RetrieveInstallmentInfoRequest;
import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

@RunWith(JMockit.class)
public class PaymentManagerTest {
    @Tested
    private PaymentManager tested;

    @Injectable
    private PaymentService paymentService;

    @Injectable
    private BasketService basketService;

    @Injectable
    private UserService userService;

    private LockService lockService;

    @Injectable
    private IyzicoProperties properties;

    private ThreadLocal<Lock> threadLocal = new ThreadLocal<>();

    private Options options;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Before
    public void setup() {
        lockService = createMockLockService();
        tested = new PaymentManager(paymentService, lockService, basketService, properties, userService);
        options = Deencapsulation.getField(tested, Options.class);
    }

    @Test
    public void constructor() {
        String apiKey = "apiKey";
        String secretKey = "secretKey";
        String baseUrl = "baseUrl";

        IyzicoProperties properties = new IyzicoProperties();
        properties.setApiKey(apiKey);
        properties.setSecretKey(secretKey);
        properties.setBaseUrl(baseUrl);

        PaymentManager paymentManager = new PaymentManager(paymentService, lockService, basketService, properties, userService);
        Options result = Deencapsulation.getField(paymentManager, Options.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(apiKey, result.getApiKey());
        Assert.assertEquals(secretKey, result.getSecretKey());
        Assert.assertEquals(baseUrl, result.getBaseUrl());
    }

    @Test(expected = PaymentException.class)
    public void getInstallments_ErrorFromServer(@Mocked User user,
                                                @Mocked Basket basket,
                                                @Mocked Lock lock) {
        long basketId = 1;
        BigDecimal basketTotal = BigDecimal.TEN;
        String digits = "123456";

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        AtomicBoolean called = new AtomicBoolean(false);

        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                Assert.assertEquals("TR", request.getLocale());
                Assert.assertEquals("TRY", request.getCurrency());
                Assert.assertEquals(digits, request.getBinNumber());
                Assert.assertEquals(basketTotal, request.getPrice());
                Assert.assertEquals(String.valueOf(basketId), request.getConversationId());
                return info.get();
            }
        }.getMockInstance();
        info.set(mock);
        mock.setStatus("error");
        mock.setErrorCode("errorCode");
        mock.setErrorMessage("errorMessage");

        new NonStrictExpectations() {{
            basket.getId();
            result = basketId;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            basketService.getUserBasket(user);
            result = basket;

            basket.getTotal();
            result = basketTotal;

            InstallmentInfo.retrieve(withInstanceOf(RetrieveInstallmentInfoRequest.class), options);

            lock.unlock();
        }};

        try {
            tested.getInstallments(user, digits);
        } catch (PaymentException e) {
            Assert.assertTrue(called.get());
            throw e;
        }
    }

    @Test(expected = PaymentException.class)
    public void getInstallments_NoAvailableInstallment(@Mocked User user,
                                                       @Mocked Basket basket,
                                                       @Mocked Lock lock) {
        long basketId = 1;
        BigDecimal basketTotal = BigDecimal.TEN;
        String digits = "123456";

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        AtomicBoolean called = new AtomicBoolean(false);

        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                Assert.assertEquals("TR", request.getLocale());
                Assert.assertEquals("TRY", request.getCurrency());
                Assert.assertEquals(digits, request.getBinNumber());
                Assert.assertEquals(basketTotal, request.getPrice());
                Assert.assertEquals(String.valueOf(basketId), request.getConversationId());
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.unmodifiableList(Collections.emptyList()));

        new NonStrictExpectations() {{
            basket.getId();
            result = basketId;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            basketService.getUserBasket(user);
            result = basket;

            basket.getTotal();
            result = basketTotal;

            InstallmentInfo.retrieve(withInstanceOf(RetrieveInstallmentInfoRequest.class), options);

            lock.unlock();
        }};

        try {
            tested.getInstallments(user, digits);
        } catch (PaymentException e) {
            Assert.assertTrue(called.get());
            throw e;
        }
    }

    @Test
    public void getInstallments(@Mocked User user,
                                @Mocked Basket basket,
                                @Mocked Lock lock) {
        long basketId = 1;
        BigDecimal basketTotal = BigDecimal.TEN;
        String digits = "123456";

        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);
        AtomicBoolean called = new AtomicBoolean(false);

        InstallmentDetail installmentDetail = new InstallmentDetail();
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                Assert.assertEquals("TR", request.getLocale());
                Assert.assertEquals("TRY", request.getCurrency());
                Assert.assertEquals(digits, request.getBinNumber());
                Assert.assertEquals(basketTotal, request.getPrice());
                Assert.assertEquals(String.valueOf(basketId), request.getConversationId());
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.unmodifiableList(Collections.singletonList(installmentDetail)));

        new NonStrictExpectations() {{
            basket.getId();
            result = basketId;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            basketService.getUserBasket(user);
            result = basket;

            basket.getTotal();
            result = basketTotal;

            InstallmentInfo.retrieve(withInstanceOf(RetrieveInstallmentInfoRequest.class), options);

            lock.unlock();
        }};

        InstallmentDetail result = tested.getInstallments(user, digits);
        Assert.assertTrue(called.get());
        Assert.assertNotNull(result);
        Assert.assertEquals(installmentDetail, result);
    }

    @Test(expected = Throwable.class)
    public void pay_UnexpectedExceptionWhileGettingProfile(@Mocked User user,
                                                           @Mocked Lock lock) {
        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;
        }};

        new Expectations() {{
            basketService.decreaseStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            paymentService.startPayment(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            basketService.rollbackStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = new Throwable();

            lock.unlock();
        }};

        tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
    }

    @Test(expected = Throwable.class)
    public void pay_UnexpectedExceptionWhileGettingBasket(@Mocked User user,
                                                          @Mocked UserProfile profile,
                                                          @Mocked Lock lock) {
        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;
        }};

        new Expectations() {{
            basketService.decreaseStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            paymentService.startPayment(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            basketService.rollbackStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = new Throwable();

            lock.unlock();
        }};

        tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
    }

    @Test(expected = InvalidInstallmentCountException.class)
    public void pay_InvalidInstallmentCount(@Mocked User user,
                                            @Mocked UserProfile profile,
                                            @Mocked Basket basket,
                                            @Mocked Lock lock) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        long basketId = 2;
        String digits = cardNumber.substring(0, 6);
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        InstallmentDetail detail = new InstallmentDetail();
        detail.setInstallmentPrices(Collections.emptyList());

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                Assert.assertEquals("TR", request.getLocale());
                Assert.assertEquals("TRY", request.getCurrency());
                Assert.assertEquals(digits, request.getBinNumber());
                Assert.assertEquals(basketTotal, request.getPrice());
                Assert.assertEquals(String.valueOf(basketId), request.getConversationId());
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;

            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;
        }};

        new Expectations() {{
            basketService.decreaseStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            paymentService.startPayment(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            basketService.rollbackStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();
            times = 2;
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = Throwable.class)
    public void pay_UnexpectedExceptionDuringBasketDecrease(@Mocked User user,
                                                            @Mocked UserProfile profile,
                                                            @Mocked Basket basket,
                                                            @Mocked Lock lock) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        long basketId = 2;
        String digits = cardNumber.substring(0, 6);
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;

            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;
        }};

        new Expectations() {{
            paymentService.startPayment(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;

            basketService.rollbackStocks(withInstanceOf(User.class), withInstanceOf(Basket.class));
            times = 0;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = new Throwable();

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = Throwable.class)
    public void pay_UnexpectedExceptionDuringStartingPayment(@Mocked User user,
                                                             @Mocked UserProfile profile,
                                                             @Mocked Basket basket,
                                                             @Mocked Lock lock) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        long basketId = 2;
        String digits = cardNumber.substring(0, 6);
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;

            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = new Throwable();

            basketService.rollbackStocks(user, basket);

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = RuntimeException.class)
    public void pay_UnexpectedExceptionDuringStockRollback(@Mocked User user,
                                                           @Mocked UserProfile profile,
                                                           @Mocked Basket basket,
                                                           @Mocked Lock lock) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        long userProfileId = 1;
        long basketId = 2;
        String digits = cardNumber.substring(0, 6);
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        new NonStrictExpectations() {{
            user.getUserProfileId();
            result = userProfileId;

            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;
        }};

        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = new Throwable();

            basketService.rollbackStocks(user, basket);
            result = new RuntimeException();

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = PaymentException.class)
    public void pay_ServiceExceptionDuringPayment(@Mocked Basket basket,
                                                  @Mocked Lock lock,
                                                  @Mocked UserPayment payment,
                                                  @Mocked Payment paymentResponse) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        User user = createUser();
        UserProfile profile = user.getProfile();
        long userProfileId = profile.getId();

        long basketId = 2;
        String digits = cardNumber.substring(0, 6);
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        long productId = Long.MAX_VALUE - 3;
        String productName = "productName";

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setPrice(BigDecimal.TEN);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProductId(productId);
        basketProduct.setProduct(product);
        basketProduct.setCount(1);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;

            basket.getProducts();
            result = products;

            paymentResponse.getStatus();
            result = "error";
        }};

        AtomicBoolean paymentCheckCalled = new AtomicBoolean(false);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = payment;

            Payment.create(with(new Delegate<CreatePaymentRequest>() {
                public boolean matches(CreatePaymentRequest request) {
                    paymentCheckCalled.set(true);
                    Assert.assertEquals(String.valueOf(basketId), request.getBasketId());
                    Assert.assertEquals(String.valueOf(basketId), request.getConversationId());
                    Assert.assertEquals("TR", request.getLocale());
                    Assert.assertEquals("TRY", request.getCurrency());
                    Assert.assertEquals(basketTotal, request.getPrice());
                    Assert.assertEquals("WEB", request.getPaymentChannel());
                    Assert.assertEquals("PRODUCT", request.getPaymentGroup());
                    Assert.assertEquals(basketTotal, request.getPaidPrice());
                    Assert.assertEquals((Integer) installment, request.getInstallment());

                    PaymentCard card = request.getPaymentCard();
                    Assert.assertNotNull(card);
                    Assert.assertEquals(holderName, card.getCardHolderName());
                    Assert.assertEquals(cardNumber, card.getCardNumber());
                    Assert.assertEquals(String.valueOf(expire.getYear()), card.getExpireYear());
                    Assert.assertEquals(String.valueOf(expire.getMonthValue()), card.getExpireMonth());
                    Assert.assertEquals(cvc, card.getCvc());
                    Assert.assertEquals((Integer) 0, card.getRegisterCard());

                    Buyer buyer = request.getBuyer();
                    Assert.assertNotNull(buyer);
                    Assert.assertEquals(profile.getCity(), buyer.getCity());
                    Assert.assertEquals(profile.getCountry(), buyer.getCountry());
                    Assert.assertEquals(profile.getEmail(), buyer.getEmail());
                    Assert.assertEquals(profile.getPhoneNumber(), buyer.getGsmNumber());
                    Assert.assertEquals(String.valueOf(user.getId()), buyer.getId());
                    Assert.assertEquals(profile.getIdentityNo(), buyer.getIdentityNumber());
                    Assert.assertEquals(ip, buyer.getIp());
                    Assert.assertEquals(formatter.format(user.getLastLoginDate()), buyer.getLastLoginDate());
                    Assert.assertEquals(profile.getName(), buyer.getName());
                    Assert.assertEquals(profile.getAddress(), buyer.getRegistrationAddress());
                    Assert.assertEquals(formatter.format(profile.getRegistrationDate()), buyer.getRegistrationDate());
                    Assert.assertEquals(profile.getSurname(), buyer.getSurname());
                    Assert.assertEquals(profile.getZipCode(), buyer.getZipCode());

                    Locale countryLocale = new Locale("", profile.getCountry());

                    Address address = request.getShippingAddress();
                    Assert.assertNotNull(address);
                    Assert.assertEquals(address, request.getBillingAddress());
                    Assert.assertEquals(String.format("%s %s", profile.getName(), profile.getSurname()), address.getContactName());
                    Assert.assertEquals(profile.getCity(), address.getCity());
                    Assert.assertEquals(countryLocale.getDisplayCountry(), address.getCountry());
                    Assert.assertEquals(profile.getAddress(), address.getAddress());
                    Assert.assertEquals(profile.getZipCode(), address.getZipCode());

                    List<BasketItem> items = request.getBasketItems();
                    Assert.assertNotNull(items);
                    Assert.assertEquals(1, items.size());
                    BasketItem item = items.get(0);
                    Assert.assertNotNull(item);
                    Assert.assertEquals(String.valueOf(productId), item.getId());
                    Assert.assertEquals(productName, item.getName());
                    Assert.assertEquals("PHYSICAL", item.getItemType());
                    Assert.assertEquals(basketTotal, item.getPrice());
                    return true;
                }
            }), options);
            result = paymentResponse;

            paymentService.markAsFailure(payment, anyString);

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = RuntimeException.class)
    public void pay_UnexpectedExceptionDuringPayment(@Mocked Basket basket,
                                                     @Mocked Lock lock,
                                                     @Mocked UserPayment payment,
                                                     @Mocked Payment paymentResponse) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        User user = createUser();
        UserProfile profile = user.getProfile();
        long userProfileId = profile.getId();

        long basketId = 2;
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        long productId = Long.MAX_VALUE - 3;
        String productName = "productName";

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setPrice(BigDecimal.TEN);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProductId(productId);
        basketProduct.setProduct(product);
        basketProduct.setCount(1);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;

            basket.getProducts();
            result = products;

            paymentResponse.getStatus();
            result = "success";
        }};

        AtomicBoolean paymentCheckCalled = new AtomicBoolean(false);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = payment;

            Payment.create(with(new Delegate<CreatePaymentRequest>() {
                public boolean matches(CreatePaymentRequest request) {
                    paymentCheckCalled.set(true);
                    return true;
                }
            }), options);
            result = new RuntimeException();

            paymentService.markAsFailure(payment, anyString);

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test(expected = RuntimeException.class)
    public void pay_UnexpectedExceptionDuringMarkingPaymentFailure(@Mocked Basket basket,
                                                                   @Mocked Lock lock,
                                                                   @Mocked UserPayment payment,
                                                                   @Mocked Payment paymentResponse) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        User user = createUser();
        UserProfile profile = user.getProfile();
        long userProfileId = profile.getId();

        long basketId = 2;
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        long productId = Long.MAX_VALUE - 3;
        String productName = "productName";

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setPrice(BigDecimal.TEN);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProductId(productId);
        basketProduct.setProduct(product);
        basketProduct.setCount(1);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;

            basket.getProducts();
            result = products;

            paymentResponse.getStatus();
            result = "success";
        }};

        AtomicBoolean paymentCheckCalled = new AtomicBoolean(false);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = payment;

            Payment.create(with(new Delegate<CreatePaymentRequest>() {
                public boolean matches(CreatePaymentRequest request) {
                    paymentCheckCalled.set(true);
                    return true;
                }
            }), options);
            result = new Throwable();

            paymentService.markAsFailure(payment, anyString);
            result = new RuntimeException();

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test
    public void pay_UnexpectedExceptionDuringMarkingPaymentSuccessfull(@Mocked Basket basket,
                                                                       @Mocked Lock lock,
                                                                       @Mocked UserPayment payment,
                                                                       @Mocked Payment paymentResponse) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        User user = createUser();
        UserProfile profile = user.getProfile();
        long userProfileId = profile.getId();

        long basketId = 2;
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        long productId = Long.MAX_VALUE - 3;
        String productName = "productName";

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setPrice(BigDecimal.TEN);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProductId(productId);
        basketProduct.setProduct(product);
        basketProduct.setCount(1);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;

            basket.getProducts();
            result = products;

            paymentResponse.getStatus();
            result = "success";
        }};

        AtomicBoolean paymentCheckCalled = new AtomicBoolean(false);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = payment;

            Payment.create(with(new Delegate<CreatePaymentRequest>() {
                public boolean matches(CreatePaymentRequest request) {
                    paymentCheckCalled.set(true);
                    return true;
                }
            }), options);
            result = paymentResponse;

            paymentService.markAsSuccess(user, payment, basket, paymentResponse);
            result = new RuntimeException();

            lock.unlock();
        }};

        try {
            tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        } finally {
            Assert.assertTrue(called.get());
        }
    }

    @Test
    public void pay(@Mocked Basket basket,
                    @Mocked Lock lock,
                    @Mocked UserPayment payment,
                    @Mocked Payment paymentResponse) {

        String holderName = "holderName";
        String cardNumber = "cardNumber";
        YearMonth expire = YearMonth.now();
        String cvc = "cvc";
        String ip = "ip";
        int installment = 1;

        User user = createUser();
        UserProfile profile = user.getProfile();
        long userProfileId = profile.getId();

        long basketId = 2;
        BigDecimal basketTotal = BigDecimal.TEN;
        Callable<Basket> callable = () -> null;
        threadLocal.set(lock);

        long productId = Long.MAX_VALUE - 3;
        String productName = "productName";

        Product product = new Product();
        product.setId(productId);
        product.setName(productName);
        product.setPrice(BigDecimal.TEN);

        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProductId(productId);
        basketProduct.setProduct(product);
        basketProduct.setCount(1);

        InstallmentDetail detail = new InstallmentDetail();
        InstallmentPrice price = new InstallmentPrice();
        price.setInstallmentNumber(installment);
        price.setInstallmentPrice(basketTotal);
        price.setTotalPrice(basketTotal);
        detail.setInstallmentPrices(Collections.singletonList(price));

        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<InstallmentInfo> info = new AtomicReference<>();
        InstallmentInfo mock = new MockUp<InstallmentInfo>() {
            @Mock
            InstallmentInfo retrieve(RetrieveInstallmentInfoRequest request, Options options) {
                if (request == null) {
                    return null;
                }

                called.set(true);
                return info.get();
            }
        }.getMockInstance();

        info.set(mock);
        mock.setStatus("success");
        mock.setInstallmentDetails(Collections.singletonList(detail));

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = basketTotal;

            basket.getId();
            result = basketId;

            basket.getProducts();
            result = products;

            paymentResponse.getStatus();
            result = "success";
        }};

        AtomicBoolean paymentCheckCalled = new AtomicBoolean(false);
        new StrictExpectations() {{
            lockService.executeInBasketLock(user, withInstanceLike(callable));

            userService.getProfileById(userProfileId);
            result = profile;

            basketService.getUserBasket(user);
            result = basket;

            lockService.executeInBasketLock(user, withInstanceLike(callable));

            lock.unlock();

            basketService.decreaseStocks(user, basket);
            result = basket;

            paymentService.startPayment(user, basket);
            result = payment;

            Payment.create(with(new Delegate<CreatePaymentRequest>() {
                public boolean matches(CreatePaymentRequest request) {
                    paymentCheckCalled.set(true);
                    return true;
                }
            }), options);
            result = paymentResponse;

            paymentService.markAsSuccess(user, payment, basket, paymentResponse);

            lock.unlock();
        }};

        Payment result = tested.pay(user, holderName, cardNumber, expire, cvc, ip, installment);
        Assert.assertNotNull(result);
        Assert.assertEquals(paymentResponse, result);
        Assert.assertTrue(called.get());
        Assert.assertTrue(paymentCheckCalled.get());
    }

    private User createUser() {
        User user = new User();
        user.setId(Long.MAX_VALUE);
        user.setActive(true);
        user.setAdmin(true);
        user.setUsername("user");
        user.setPassword("user");
        user.setLastLoginDate(LocalDateTime.now());
        user.setLastSessionKey("lastSessionKey");
        user.setUserProfileId(Long.MAX_VALUE - 1);

        UserProfile profile = new UserProfile();
        profile.setAddress("address");
        profile.setCity("city");
        profile.setCountry("country");
        profile.setEmail("email");
        profile.setId(user.getUserProfileId());
        profile.setIdentityNo("identityNo");
        profile.setName("name");
        profile.setPhoneNumber("phoneNumber");
        profile.setRegistrationDate(user.getLastLoginDate());
        profile.setSurname("surname");
        profile.setZipCode("zipCode");
        user.setProfile(profile);
        return user;
    }

    private LockService createMockLockService() {
        return new MockUp<LockService>() {
            @Mock
            public BLock getProductLock(Long productId) {
                return null;
            }

            @Mock
            public <T> T executeInBasketLock(User user, Callable<T> callable) {
                try {
                    return callable.call();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    threadLocal.get().unlock();
                }
            }
        }.getMockInstance();
    }
}