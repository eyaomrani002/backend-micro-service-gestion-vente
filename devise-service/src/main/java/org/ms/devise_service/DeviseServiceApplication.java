package org.ms.devise_service;

import org.ms.devise_service.entities.Devise;
import org.ms.devise_service.repository.DeviseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class DeviseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviseServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(DeviseRepository deviseRepository) {
        return args -> {
            if (deviseRepository.count() == 0) {
                deviseRepository.save(new Devise(null, "MAD", "Dirham marocain", 1.0, true));
                deviseRepository.save(new Devise(null, "USD", "Dollar US", 9.5, false));
                deviseRepository.save(new Devise(null, "EUR", "Euro", 10.2, false));
                deviseRepository.save(new Devise(null, "TND", "Dinar tunisien", 3.3, false));
            }
        };
    }
}
