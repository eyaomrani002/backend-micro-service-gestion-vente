package org.ms.client_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {
    private Long id;
    private Date dateFacture;
    private Double total;
    private Double resteAPayer; // Ajout pour cohérence avec l'endpoint reste-a-payer
    private String status; // PAYEE, NON_PAYEE, PARTIELLEMENT_PAYEE
    private Long clientId;
}

