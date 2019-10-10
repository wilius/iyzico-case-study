package com.iyzico.challenge.integrator.controller.management;

import com.iyzico.challenge.integrator.data.service.ProductService;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import com.iyzico.challenge.integrator.dto.product.request.CreateProductRequest;
import com.iyzico.challenge.integrator.mapper.ProductMapper;
import com.iyzico.challenge.integrator.session.AdminEndpoint;
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
@AdminEndpoint
@RequestMapping("management/product")
public class ProductController {

    private final ProductMapper mapper;
    private final ProductService service;

    public ProductController(ProductMapper mapper,
                             ProductService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @ApiOperation(
            value = "Create Product",
            notes = "Creates a new product"
    )
    @RequestMapping(method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public ProductDto createProduct(@RequestBody @Validated CreateProductRequest request,
                                    @ApiIgnore @IntegratorSession ApiSession session) {
        return mapper.map(service.create(session.getUser(), request.getName(), request.getStockCount(), request.getPrice()));
    }
}
