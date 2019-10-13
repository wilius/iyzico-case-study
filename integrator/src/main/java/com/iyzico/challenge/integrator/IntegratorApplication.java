package com.iyzico.challenge.integrator;

import com.iyzico.challenge.integrator.properties.IyzicoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
        IyzicoProperties.class
})
@SpringBootApplication(exclude = {
        GsonAutoConfiguration.class
})
public class IntegratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegratorApplication.class, args);
    }
}