package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.LongText;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RunWith(JMockit.class)
public class ProductMapperTest {
    @Tested
    private ProductMapper tested;

    @Test
    public void map() {
        Product product = createProduct(1, "name", "barcode", 10, BigDecimal.ONE, "description");

        ProductDto result = tested.map(product);
        Assert.assertNotNull(result);
        Assert.assertEquals(product.getId(), result.getId());
        Assert.assertEquals(product.getName(), result.getName());
        Assert.assertEquals(product.getBarcode(), result.getBarcode());
        Assert.assertEquals(product.getStockCount(), result.getStockCount());
        Assert.assertEquals(product.getPrice(), result.getPrice());
        Assert.assertNull(result.getDescription());
    }

    @Test
    public void map_iterable() {
        Product product = createProduct(1, "name", "barcode", 10, BigDecimal.ONE, "description");

        Iterable<Product> products = Collections.singleton(product);

        List<ProductDto> result = tested.map(products);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        ProductDto resultProduct = result.get(0);
        Assert.assertEquals(product.getId(), resultProduct.getId());
        Assert.assertEquals(product.getName(), resultProduct.getName());
        Assert.assertEquals(product.getBarcode(), resultProduct.getBarcode());
        Assert.assertEquals(product.getStockCount(), resultProduct.getStockCount());
        Assert.assertEquals(product.getPrice(), resultProduct.getPrice());
        Assert.assertNull(resultProduct.getDescription());
    }

    @Test
    public void mapWithDescription() {

        Product product = createProduct(1, "name", "barcode", 10, BigDecimal.ONE, "description");

        ProductDto result = tested.mapWithDescription(product);
        Assert.assertNotNull(result);
        Assert.assertEquals(product.getId(), result.getId());
        Assert.assertEquals(product.getName(), result.getName());
        Assert.assertEquals(product.getBarcode(), result.getBarcode());
        Assert.assertEquals(product.getStockCount(), result.getStockCount());
        Assert.assertEquals(product.getPrice(), result.getPrice());
        Assert.assertEquals(product.getDescription().getContent(), result.getDescription());
    }

    private Product createProduct(long id, String name, String barcode, long stockCount, BigDecimal price, String description) {
        Product product = new Product();
        product.setId(id);
        product.setStockCount(stockCount);
        product.setBarcode(barcode);
        product.setStatus(stockCount > 0 ? Product.Status.IN_STOCK : Product.Status.OUT_OF_STOCK);
        product.setPrice(price);
        product.setName(name);
        if (StringUtils.isNotEmpty(description)) {
            LongText desc = new LongText();
            desc.setTable(Product.TABLE_NAME);
            desc.setColumnName(Product.DESCRIPTION_COLUMN_NAME);
            desc.setRecordId(String.valueOf(id));
            desc.setContent(description);

            product.setDescription(desc);
        }
        return product;
    }
}