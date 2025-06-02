package org.ms.reglement_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Devise {
    private Long id;
    private String code; // EUR, USD, MAD, etc.
    private String name;
    private double tauxChange; // Exchange rate relative to MAD
    private boolean deviseReference; // True for reference currency
}