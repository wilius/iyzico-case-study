package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.dto.basket.AddProductToTheBasketRequest;
import com.iyzico.challenge.integrator.dto.basket.BasketDto;
import com.iyzico.challenge.integrator.mapper.BasketMapper;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import com.iyzico.challenge.integrator.session.annotation.IntegratorSession;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@SecuredEndpoint
@RequestMapping("basket")
public class BasketController {
    private final BasketService service;
    private final BasketMapper mapper;

    public BasketController(BasketService service,
                            BasketMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @ApiOperation(
            value = "Add to Basket",
            notes = "Adds given item to the basket"
    )
    @RequestMapping(method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void add(@RequestBody @Validated AddProductToTheBasketRequest request,
                    @ApiIgnore @IntegratorSession ApiSession session) {

        service.addItem(session.getUser(), request.getProductId(), request.getCount());
    }

    @ApiOperation(
            value = "Delete from Basket",
            notes = "Deletes given item from the basket"
    )
    @RequestMapping(value = "/{basketProductId}", method = RequestMethod.DELETE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void delete(@PathVariable long basketProductId,
                       @ApiIgnore @IntegratorSession ApiSession session) {

        service.deleteItem(session.getUser(), basketProductId);
    }

    @ApiOperation(
            value = "Get Basket",
            notes = "Gets current basket with all of the items in it"
    )
    @RequestMapping(method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Throwable.class, readOnly = true)
    public BasketDto get(@ApiIgnore @IntegratorSession ApiSession session) {
        return mapper.map(
                service.getUserBasket(session.getUser())
        );
    }

}
