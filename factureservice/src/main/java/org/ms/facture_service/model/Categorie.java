package org.ms.facture_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {
    private Long id;
    private String name;
    private String description;
}