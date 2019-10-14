package com.iyzico.challenge.payment.service;

import com.iyzico.challenge.payment.entity.Payment;
import com.iyzico.challenge.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

@Service
@Transactional
public class IyzicoPaymentService {

    private Logger logger = LoggerFactory.getLogger(IyzicoPaymentService.class);

    private BankService bankService;
    private PaymentRepository paymentRepository;

    private final TransactionTemplate requireNewTransactionTemplate;

    public IyzicoPaymentService(BankService bankService,
                                PaymentRepository paymentRepository,
                                PlatformTransactionManager transactionManager) {
        this.bankService = bankService;
        this.paymentRepository = paymentRepository;

        requireNewTransactionTemplate = new TransactionTemplate(transactionManager);
        requireNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(propagation = Propagation.NEVER)
    public void pay(BigDecimal price) {
        Payment payment = requireNewTransactionTemplate.execute(x -> {
            Payment inner = new Payment();
            inner.setPrice(price);
            inner.setStatus(Payment.Status.IN_PROGRESS);
            return paymentRepository.saveAndFlush(inner);
        });

        BankPaymentRequest request = new BankPaymentRequest();
        request.setPrice(price);
        BankPaymentResponse response = null;

        Payment.Status status;
        String message = null;
        try {
            response = bankService.pay(request);
            status = Payment.Status.SUCCESS;
        } catch (Throwable t) {
            logger.warn("Unexpected exception during the payment", t);
            message = t.getMessage();
            status = Payment.Status.ERROR;
        }

        try {
            String finalMessage = message;
            BankPaymentResponse finalResponse = response;
            Payment.Status finalStatus = status;

            for (int i = 0; i < 10; i++) {
                try {
                    requireNewTransactionTemplate.execute(x -> {
                        payment.setBankResponse(finalResponse == null ? finalMessage : finalResponse.getResultCode());
                        payment.setStatus(finalStatus);
                        return paymentRepository.saveAndFlush(payment);
                    });

                    break;
                } catch (Throwable t) {
                    if (i == 9) {
                        throw t;
                    }
                }
            }
        } catch (Throwable t) {
            // queue it to retry or send a notification to the operation department to review it and fix the problem manually
            logger.warn("Unexpected exception while saving state of payment. status: {}", status, t);
            throw t;
        }

        logger.info("Payment saved successfully!");
    }
}
