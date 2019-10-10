package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.dto.selling.AddProductToTheBasketRequest;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import com.iyzico.challenge.integrator.session.annotation.IntegratorSession;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@SecuredEndpoint
@RequestMapping("basket")
public class BasketController {
    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @ApiOperation(
            value = "Add to Basket",
            notes = "Adds given item to the basket"
    )
    @RequestMapping(name = "/add/{productId}/{count}", method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void add(@RequestBody @Validated AddProductToTheBasketRequest request,
                    @ApiIgnore @IntegratorSession ApiSession session) {

        basketService.addItem(session.getUser(), request.getProductId(), request.getCount());
    }

    @ApiOperation(
            value = "Get Basket",
            notes = "Gets current basket with all of the items in it"
    )
    @RequestMapping(method = RequestMethod.GET)
    public void get(@ApiIgnore @IntegratorSession ApiSession session) {

    }

}
