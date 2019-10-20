package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.dto.payment.InstallmentDto;
import com.iyzico.challenge.integrator.dto.payment.PaymentRequest;
import com.iyzico.challenge.integrator.service.PaymentManager;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzico.challenge.integrator.util.HttpUtils;
import com.iyzipay.model.InstallmentDetail;
import com.iyzipay.model.InstallmentPrice;
import com.iyzipay.model.Payment;
import mockit.StrictExpectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;

@RunWith(JMockit.class)
public class PaymentControllerTest {

    @Tested
    private PaymentController tested;

    @Injectable
    private PaymentManager service;

    @Test
    public void getInstallments(@Mocked ApiSession session,
                                @Mocked User user) {
        String digits = "123456";

        Long bankCode = Long.MAX_VALUE;
        String bankName = "bankName";
        String cardAssociation = "cardAssociation";
        String cardType = "cardType";

        InstallmentDetail detail = new InstallmentDetail();
        detail.setBankCode(bankCode);
        detail.setBankName(bankName);
        detail.setCardAssociation(cardAssociation);
        detail.setCardType(cardType);
        detail.setForce3ds(1);
        detail.setCommercial(0);
        detail.setForceCvc(-1);

        int installmentNumber = 12;
        BigDecimal price = BigDecimal.TEN;
        BigDecimal total = price.multiply(new BigDecimal(installmentNumber));

        InstallmentPrice installmentPrice = new InstallmentPrice();
        installmentPrice.setInstallmentNumber(installmentNumber);
        installmentPrice.setInstallmentPrice(price);
        installmentPrice.setTotalPrice(total);

        detail.setInstallmentPrices(Collections.singletonList(installmentPrice));

        new StrictExpectations() {{
            session.getUser();
            result = user;

            service.getInstallments(user, digits);
            result = detail;
        }};

        InstallmentDto result = tested.getInstallments(digits, session);
        Assert.assertEquals(bankCode, result.getBankCode());
        Assert.assertEquals(bankName, result.getBankName());
        Assert.assertEquals(cardAssociation, result.getCardAssociation());
        Assert.assertEquals(cardType, result.getCardType());
        Assert.assertNotNull(result.getPrices());
        Assert.assertTrue(result.isForce3ds());
        Assert.assertTrue(result.isForceCvc());
        Assert.assertFalse(result.isCommercial());

        Assert.assertEquals(1, result.getPrices().size());

        InstallmentDto.InstallmentPrice item = result.getPrices().get(0);
        Assert.assertEquals(installmentNumber, item.getCount());
        Assert.assertEquals(price, item.getInstallment());
        Assert.assertEquals(total, item.getTotal());
    }

    @Test
    public void pay(@Mocked ApiSession session,
                    @Mocked User user,
                    @Mocked HttpServletRequest servletRequest,
                    @Mocked Payment payment) {

        String clientIp = "clientIp";

        String cardHolder = "cardHolder";
        String cardNumber = "cardNumber";
        String cvc = "cvc";
        YearMonth expire = YearMonth.now().plusYears(4);
        int installment = 3;

        PaymentRequest request = new PaymentRequest();
        request.setHolderName(cardHolder);
        request.setCardNumber(cardNumber);
        request.setCvc(cvc);
        request.setExpire(expire);
        request.setInstallment(installment);

        new StrictExpectations(HttpUtils.class) {{
            session.getUser();
            result = user;

            HttpUtils.getClientIp(servletRequest);
            result = clientIp;

            service.pay(user, cardHolder, cardNumber, expire, cvc, clientIp, installment);
            result = payment;
        }};

        Payment result = tested.pay(request, session, servletRequest);
        Assert.assertEquals(payment, result);
    }
}