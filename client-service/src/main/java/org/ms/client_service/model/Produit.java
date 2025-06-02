package org.ms.client_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produit {
    private Long id;
    private String name;
    private Double price;
    private Long quantity;
    private Categorie categorie; // Added to match produit-service
    private Long version; // Added for completeness
}