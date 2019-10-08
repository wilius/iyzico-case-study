package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.dto.BadRequestResponse;
import com.iyzico.challenge.integrator.dto.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class RestErrorController implements ErrorController {
    private final static Logger log = LoggerFactory.getLogger(RestErrorController.class);
    private final static String ERROR_PATH = "/error";

    @RequestMapping(path = ERROR_PATH)
    public BadRequestResponse error(Exception e) {
        log.warn(e.getMessage(), e);
        return new BadRequestResponse(ErrorCode.API_NOT_FOUND, "Api requested not found");
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}

