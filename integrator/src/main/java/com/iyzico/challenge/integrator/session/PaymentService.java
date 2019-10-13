package com.iyzico.challenge.integrator.session;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.exception.PaymentException;
import com.iyzico.challenge.integrator.properties.IyzicoProperties;
import com.iyzico.challenge.integrator.service.hazelcast.LockService;
import com.iyzipay.IyzipayResource;
import com.iyzipay.Options;
import com.iyzipay.model.Currency;
import com.iyzipay.model.InstallmentDetail;
import com.iyzipay.model.InstallmentInfo;
import com.iyzipay.model.Locale;
import com.iyzipay.request.RetrieveInstallmentInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final LockService lockService;
    private final BasketService basketService;
    private final Options options;

    public PaymentService(LockService lockService,
                          BasketService basketService,
                          IyzicoProperties properties) {
        this.lockService = lockService;
        this.basketService = basketService;

        Options options = new Options();
        options.setBaseUrl(properties.getBaseUrl());
        options.setApiKey(properties.getApiKey());
        options.setSecretKey(properties.getSecretKey());
        this.options = options;
    }

    public InstallmentDetail getInstallments(User user, String digits) {
        InstallmentInfo result = lockService.executeInBasketLock(user, () -> {
            Basket basket = basketService.getByUser(user);
            RetrieveInstallmentInfoRequest request = new RetrieveInstallmentInfoRequest();

            request.setLocale(Locale.TR.getValue());
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

    private void validate(IyzipayResource result) {
        if (!"success".equalsIgnoreCase(result.getStatus())) {
            throw new PaymentException(String.format("%s: %s", result.getErrorCode(), result.getErrorMessage()));
        }
    }
}
