package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.dto.payment.InstallmentDto;
import com.iyzico.challenge.integrator.session.PaymentService;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import com.iyzico.challenge.integrator.session.annotation.IntegratorSession;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzipay.model.InstallmentDetail;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.stream.Collectors;

@RestController
@SecuredEndpoint
@RequestMapping("payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ApiOperation(
            value = "Get Installment Info",
            notes = "Gets the installment info for given card"
    )
    @RequestMapping(value = "installments/{digits:.{6}}", method = RequestMethod.GET)
    public InstallmentDto get(@PathVariable("digits") String digits,
                              @ApiIgnore @IntegratorSession ApiSession session) {
        InstallmentDetail details = paymentService.getInstallments(session.getUser(), digits);
        return map(details);
    }

    private InstallmentDto map(InstallmentDetail details) {
        InstallmentDto dto = new InstallmentDto();
        dto.setCardType(details.getCardType());
        dto.setCardAssociation(details.getCardAssociation());
        dto.setBankCode(details.getBankCode());
        dto.setBankName(details.getBankName());
        dto.setForce3ds(details.getForce3ds() > 0);
        dto.setCommercial(details.getCommercial() > 0);
        dto.setForceCvc(details.getForceCvc() > 0);
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
}
