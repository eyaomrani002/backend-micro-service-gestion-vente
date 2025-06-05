package org.ms.facture_service;

import org.ms.facture_service.entities.Facture;
import org.ms.facture_service.entities.FactureLigne;
import org.ms.facture_service.feign.ClientServiceClient;
import org.ms.facture_service.feign.ProduitServiceClient;
import org.ms.facture_service.model.Client;
import org.ms.facture_service.model.Produit;
import org.ms.facture_service.repository.FactureLigneRepository;
import org.ms.facture_service.repository.FactureRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class FactureServiceApplication {
    private static final Logger LOGGER = Logger.getLogger(FactureServiceApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(FactureServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(FactureRepository factureRepository, FactureLigneRepository factureLigneRepository,
                            ClientServiceClient clientServiceClient, ProduitServiceClient produitServiceClient,
                            RepositoryRestConfiguration restConfiguration) {
        return args -> {
            restConfiguration.exposeIdsFor(Facture.class, FactureLigne.class);
            // ATTENTION : Ne pas appeler de méthode sécurisée par @PreAuthorize ici !
            // Pour l'initialisation, créer un client et des produits factices localement si besoin.
            // Exemple sans appel Feign sécurisé :
            if (factureRepository.count() > 0) return;
            Client client = new Client(1L, "Client Test", "test@mail.com", "Adresse");
            Facture facture = new Facture(new Date(), "NON_PAYEE", client.getId());
            facture.setClient(client);
            facture = factureRepository.save(facture);

            // Crée des produits factices si besoin, ou désactive cette partie si dépendance à produit-service
            // var produits = ... (optionnel)
            // if (produits != null) { ... }

            facture = factureRepository.findById(facture.getId()).orElse(facture);
            factureRepository.save(facture);
        };
    }
}
