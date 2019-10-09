package com.iyzico.challenge.integrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

import static com.iyzico.challenge.integrator.session.model.ApiSession.HEADER_SESSION_KEY;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        List<Parameter> globalOperationParameters = new ArrayList<>();
        globalOperationParameters.add(new ParameterBuilder()
                .name(HEADER_SESSION_KEY)
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build());

        return new Docket(DocumentationType.SWAGGER_2)
                .enable(true)
                .globalOperationParameters(globalOperationParameters)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.iyzico.challenge.integrator"))
                .paths(PathSelectors.any())
                .build();
    }
}
