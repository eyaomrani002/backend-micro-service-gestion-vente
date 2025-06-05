package org.ms.facture_service.feign;

import org.ms.facture_service.model.Produit;
import org.ms.facture_service.security.FeignClientConfig;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@FeignClient(name="PRODUIT-SERVICE", configuration = FeignClientConfig.class)
public interface ProduitServiceClient {
    @GetMapping("/produits")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    PagedModel<Produit> getAllProduits(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/produits/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    Produit getProduitById(@PathVariable("id") Long id);

    @PutMapping("/produits/{id}/decreaseStock")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Produit> decreaseStock(@PathVariable Long id, @RequestParam int quantity);
}

@Component
class ProduitServiceClientFallbackFactory implements FallbackFactory<ProduitServiceClient> {
    @Override
    public ProduitServiceClient create(Throwable cause) {
        return new ProduitServiceClient() {
            @Override
            public PagedModel<Produit> getAllProduits(int page, int size) {
                System.err.println("Fallback triggered for getAllProduits: " + cause.getMessage());
                return PagedModel.of(Collections.emptyList(), new PagedModel.PageMetadata(size, page, 0, 0));
            }

            @Override
            public Produit getProduitById(Long id) {
                System.err.println("Fallback triggered for getProduitById: " + cause.getMessage());
                return new Produit(id, "Produit indisponible", 0.0, 0, null, null);
            }
            @Override
            public ResponseEntity<Produit> decreaseStock(Long id, int quantity) {
                System.err.println("Fallback triggered for decreaseStock with id " + id + ", quantity " + quantity + ": " + cause.getMessage());
                // Retourner une réponse d'erreur pour indiquer que l'opération n'a pas pu être effectuée
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }

        };
    }
}