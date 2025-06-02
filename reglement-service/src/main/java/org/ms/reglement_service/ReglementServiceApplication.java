package org.ms.reglement_service;

import org.ms.reglement_service.entities.Reglement;
import org.ms.reglement_service.feign.FactureServiceClient;
import org.ms.reglement_service.feign.DeviseServiceClient;
import org.ms.reglement_service.model.Facture;
import org.ms.reglement_service.model.Devise;
import org.ms.reglement_service.repository.ReglementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ReglementServiceApplication {
    private static final Logger LOGGER = Logger.getLogger(ReglementServiceApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(ReglementServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReglementRepository reglementRepository,
                           FactureServiceClient factureServiceClient,
                           RepositoryRestConfiguration restConfiguration) {
        restConfiguration.exposeIdsFor(Reglement.class);
        return args -> {
            try {
                Facture testFacture = factureServiceClient.findFactureById(1L);
                if (testFacture != null) {
                    Reglement r1 = new Reglement(null, 1L, 500.0, new Date(), "MAD", "PAY-123", "COMPLET");
                    Reglement r2 = new Reglement(null, 1L, 300.0, new Date(), "EUR", "PAY-456", "PARTIEL");
                    reglementRepository.saveAll(Arrays.asList(r1, r2));
                    LOGGER.info("=== Initial Reglements Created ===");
                    reglementRepository.findAll().forEach(r -> LOGGER.info(r.toString()));
                } else {
                    LOGGER.warning("Facture with ID 1 not found. Skipping reglement initialization.");
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to initialize reglements: " + e.getMessage());
            }
        };
    }

    @Component
    class FactureServiceClientFallbackFactory implements FallbackFactory<FactureServiceClient> {
        @Override
        public FactureServiceClient create(Throwable cause) {
            return new FactureServiceClient() {
                @Override
                public Facture findFactureById(Long id) {
                    LOGGER.warning("Fallback: Facture service unavailable: " + cause.getMessage());
                    return null;
                }
                @Override
                public void updateFactureStatus(Long id, String status) {}
                @Override
                public List<Long> getFactureIdsByClient(Long clientId) {
                    return Collections.emptyList();
                }
                @Override
                public Double getFactureTotal(Long id) {
                    return 0.0;
                }
                @Override
                public void updateFactureMontantPaye(Long id, Double montantPaye) {}
            };
        }
    }

    @Component
    class DeviseServiceClientFallbackFactory implements FallbackFactory<DeviseServiceClient> {
        @Override
        public DeviseServiceClient create(Throwable cause) {
            return new DeviseServiceClient() {
                @Override
                public Devise findDeviseByCode(String code) {
                    LOGGER.warning("Fallback: Devise service unavailable: " + cause.getMessage());
                    return new Devise(null, code, "Unknown", 1.0, false);
                }
            };
        }
    }
}