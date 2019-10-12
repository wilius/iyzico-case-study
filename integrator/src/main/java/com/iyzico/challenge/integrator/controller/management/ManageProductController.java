package com.iyzico.challenge.integrator.controller.management;

import com.iyzico.challenge.integrator.data.service.ProductService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import com.iyzico.challenge.integrator.dto.product.request.CreateProductRequest;
import com.iyzico.challenge.integrator.dto.product.request.UpdateProductRequest;
import com.iyzico.challenge.integrator.mapper.ProductMapper;
import com.iyzico.challenge.integrator.session.AdminEndpoint;
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
@AdminEndpoint
@RequestMapping("management/product")
public class ManageProductController {

    private final ProductMapper mapper;
    private final ProductService service;

    public ManageProductController(ProductMapper mapper,
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
    public ProductDto create(@RequestBody @Validated CreateProductRequest request,
                             @ApiIgnore @IntegratorSession ApiSession session) {
        return mapper.map(service.create(session.getUser(), request.getBarcode(), request.getName(), request.getStockCount(), request.getPrice(), request.getDescription()));
    }

    @ApiOperation(
            value = "Update Product",
            notes = "Updates an existing product"
    )
    @RequestMapping(method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public ProductDto update(@RequestBody @Validated UpdateProductRequest request) {
        return mapper.map(service.update(request.getId(), request.getBarcode(), request.getName(), request.getStockCount(), request.getPrice(), request.getDescription()));
    }

    @ApiOperation(
            value = "Publish Product",
            notes = "Publishes an existing product for sale"
    )
    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void publish(@PathVariable("id") long id) {
        service.publish(id);
    }

    @ApiOperation(
            value = "Unpublish Product",
            notes = "Unpublishes an existing product"
    )
    @RequestMapping(value = "/{id}/unpublish", method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void unpublish(@PathVariable("id") long id) {
        service.unpublish(id);
    }

    @ApiOperation(
            value = "Get Product",
            notes = "Gets an existing product"
    )
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public ProductDto get(@PathVariable("id") long id) {
        return mapper.map(service.getById(id));
    }

    @ApiOperation(
            value = "Get All Product",
            notes = "Gets all of the existing products"
    )
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public ListResponse<ProductDto> getAll() {
        return new ListResponse<>(
                mapper.map(service.getAllItems())
        );
    }

    @ApiOperation(
            value = "Get Unpublised Product",
            notes = "Gets all of the unpublished products"
    )
    @RequestMapping(value = "/all/unpublised", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public ListResponse<ProductDto> getUnpublishedProducts() {
        return new ListResponse<>(
                mapper.map(service.getAllUnpublishedItems())
        );
    }

}
