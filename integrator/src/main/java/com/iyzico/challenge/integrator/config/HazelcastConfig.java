package com.iyzico.challenge.integrator.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;

@Configuration
public class HazelcastConfig {

    @Bean
    public HazelcastInstance hazelcast() throws IOException {
        URL xmlConfigUrl = HazelcastConfig.class.getClassLoader().getResource("hazelcast.xml");
        if (xmlConfigUrl == null) {
            throw new RuntimeException("hazelcast.xml not found in classpath");
        }

        Config config = new XmlConfigBuilder(xmlConfigUrl).build();
        return Hazelcast.newHazelcastInstance(config);
    }
}