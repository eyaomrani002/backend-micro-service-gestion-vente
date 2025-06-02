package org.ms.produit_service;

import org.ms.produit_service.entities.Categorie;
import org.ms.produit_service.entities.Produit;
import org.ms.produit_service.repository.CategorieRepository;
import org.ms.produit_service.repository.ProduitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

@SpringBootApplication
@EnableFeignClients
public class ProduitServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProduitServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ProduitRepository produitRepository, CategorieRepository categorieRepository, 
                           RepositoryRestConfiguration repositoryRestConfiguration) {
        repositoryRestConfiguration.exposeIdsFor(Produit.class, Categorie.class);
        return args -> {
            if (categorieRepository.count() == 0) {
                categorieRepository.save(new Categorie(null, "PC", "Ordinateurs personnels"));
                categorieRepository.save(new Categorie(null, "Imprimante", "Imprimantes et scanners"));
                categorieRepository.save(new Categorie(null, "Smartphone", "Téléphones intelligents"));
                categorieRepository.save(new Categorie(null, "Accessoires", "Souris, claviers, casques"));
            }
            if (produitRepository.count() == 0) {
                Categorie pc = categorieRepository.findByName("PC").orElse(null);
                Categorie imprimante = categorieRepository.findByName("Imprimante").orElse(null);
                Categorie smartphone = categorieRepository.findByName("Smartphone").orElse(null);
                Categorie accessoires = categorieRepository.findByName("Accessoires").orElse(null);

                produitRepository.save(new Produit(null, "Dell Inspiron 15", 750, 50, pc, null));
                produitRepository.save(new Produit(null, "HP LaserJet Pro", 300, 30, imprimante, null));
                produitRepository.save(new Produit(null, "iPhone 14", 1200, 25, smartphone, null));
                produitRepository.save(new Produit(null, "Logitech MX Master 3", 100, 100, accessoires, null));
                produitRepository.save(new Produit(null, "Samsung Galaxy S23", 999, 40, smartphone, null));
                produitRepository.save(new Produit(null, "Canon Pixma TS8350", 150, 15, imprimante, null));
            }
            produitRepository.findAll().forEach(p -> {
                System.out.println(p.getName() + ":" + p.getPrice() + ":" + p.getQuantity() + ":" + 
                    (p.getCategorie() != null ? p.getCategorie().getName() : "No Category"));
            });
        };
    }

}