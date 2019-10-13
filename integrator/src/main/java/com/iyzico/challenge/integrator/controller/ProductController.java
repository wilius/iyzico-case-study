package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.service.ProductService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import com.iyzico.challenge.integrator.mapper.ProductMapper;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecuredEndpoint
@RequestMapping("product")
public class ProductController {
    private final ProductMapper mapper;
    private final ProductService service;

    public ProductController(ProductMapper mapper,
                             ProductService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @ApiOperation(
            value = "Get Product",
            notes = "Gets an existing product"
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Throwable.class, readOnly = true)
    public ProductDto get(@PathVariable("id") long id) {
        return mapper.mapWithDescription(service.getPublishedItem(id));
    }

    @ApiOperation(
            value = "Get All Product",
            notes = "Gets all of the existing products"
    )
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ListResponse<ProductDto> getAll() {
        return new ListResponse<>(
                mapper.map(service.getAllPublishedItems())
        );
    }
}
