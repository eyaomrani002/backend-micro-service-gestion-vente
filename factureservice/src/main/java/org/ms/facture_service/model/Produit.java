package org.ms.facture_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Produit {
    private Long id;
    private String name;
    private double price;
    private Integer quantity;
    private Categorie categorie; // Added for produit-service compatibility
    private Long version; // Added for completeness
}