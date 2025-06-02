package org.ms.reglement_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Reglement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Facture ID cannot be null")
    private Long factureId;

    @Positive(message = "Montant must be positive")
    private Double montant;

    @NotNull(message = "Date de règlement cannot be null")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateReglement;

    @NotBlank(message = "Mode de paiement cannot be empty")
    private String modePaiement;

    @NotBlank(message = "Reference cannot be empty")
    private String reference;

    @NotBlank(message = "Statut cannot be empty")
    private String statut; // COMPLET, PARTIEL, ANNULE
}
