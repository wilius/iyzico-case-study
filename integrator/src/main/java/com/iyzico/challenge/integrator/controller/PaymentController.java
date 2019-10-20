package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.dto.payment.InstallmentDto;
import com.iyzico.challenge.integrator.dto.payment.PaymentRequest;
import com.iyzico.challenge.integrator.service.PaymentManager;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import com.iyzico.challenge.integrator.session.annotation.IntegratorSession;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzico.challenge.integrator.util.HttpUtils;
import com.iyzipay.model.InstallmentDetail;
import com.iyzipay.model.Payment;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@SecuredEndpoint
@RequestMapping("payment")
public class PaymentController {
    private final PaymentManager paymentManager;

    public PaymentController(PaymentManager paymentManager) {
        this.paymentManager = paymentManager;
    }

    @ApiOperation(
            value = "Get Installment Info",
            notes = "Gets the installment info for given card"
    )
    @RequestMapping(value = "installments/{digits:.{6}}", method = RequestMethod.GET)
    public InstallmentDto getInstallments(@PathVariable("digits") String digits,
                                          @ApiIgnore @IntegratorSession ApiSession session) {
        InstallmentDetail details = paymentManager.getInstallments(session.getUser(), digits);
        return map(details);
    }

    @ApiOperation(
            value = "Pay",
            notes = "Makes the payment with given details"
    )
    @RequestMapping(method = RequestMethod.PUT)
    public Payment pay(@RequestBody @Valid PaymentRequest request,
                       @ApiIgnore @IntegratorSession ApiSession session,
                       @ApiIgnore HttpServletRequest servletRequest) {

        return paymentManager.pay(
                session.getUser(),
                request.getHolderName(),
                request.getCardNumber(),
                request.getExpire(),
                request.getCvc(),
                HttpUtils.getClientIp(servletRequest),
                request.getInstallment());
    }


    private InstallmentDto map(InstallmentDetail details) {
        InstallmentDto dto = new InstallmentDto();
        dto.setCardType(details.getCardType());
        dto.setCardAssociation(details.getCardAssociation());
        dto.setBankCode(details.getBankCode());
        dto.setBankName(details.getBankName());
        dto.setForce3ds(int2bool(details.getForce3ds()));
        dto.setCommercial(int2bool(details.getCommercial()));
        dto.setForceCvc(int2bool(details.getForceCvc()));
        dto.setPrices(details.getInstallmentPrices()
                .stream()
                .map(x -> {
                    InstallmentDto.InstallmentPrice price = new InstallmentDto.InstallmentPrice();
                    price.setInstallment(x.getInstallmentPrice());
                    price.setTotal(x.getTotalPrice());
                    price.setCount(x.getInstallmentNumber());
                    return price;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    private boolean int2bool(Integer integer) {
        return integer != null && integer != 0;
    }
}
