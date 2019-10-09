package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecuredEndpoint
@RequestMapping("basket")
public class BasketController {
    @RequestMapping(path = "/test/{name}", method = RequestMethod.GET)
    public String search(@PathVariable(value = "name") String name) {
        return String.format("Hello, %s", name);
    }
}
