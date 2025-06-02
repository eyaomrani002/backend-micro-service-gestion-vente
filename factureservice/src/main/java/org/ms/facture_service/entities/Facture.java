package org.ms.facture_service.entities;

import lombok.*;
import org.ms.facture_service.model.Client;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
public class Facture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dateFacture;

    private String status; // NON_PAYEE, PARTIELLEMENT_PAYEE, PAYEE

    private Double total;

    private Double resteAPayer;

    private Double montantPaye;  

    @OneToMany(mappedBy = "facture", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<FactureLigne> factureLignes;

    @Transient
    private Client client;

    @Column(name = "client_id")
    private Long clientID;

    public Facture(Date dateFacture, String status, Long clientID) {
        this.dateFacture = dateFacture;
        this.status = status;
        this.clientID = clientID;
        this.total = 0.0;
        this.resteAPayer = 0.0;
        this.montantPaye = 0.0;
    }

    @PrePersist @PreUpdate
    public void computeTotals() {
        if (factureLignes != null && !factureLignes.isEmpty()) {
            this.total = factureLignes.stream()
                    .mapToDouble(fl -> fl.getPrice() * fl.getQuantity())
                    .sum();
        } else {
            this.total = 0.0;
        }

        if (montantPaye == null) {
            montantPaye = 0.0;
        }

        this.resteAPayer = total - montantPaye;

        // Mise à jour du statut selon montant payé
        if (resteAPayer <= 0) {
            this.status = "PAYEE";
            this.resteAPayer = 0.0;
        } else if (montantPaye > 0) {
            this.status = "PARTIELLEMENT_PAYEE";
        } else {
            this.status = "NON_PAYEE";
        }

        // Assure que les lignes référencent bien cette facture
        if (factureLignes != null) {
            for (FactureLigne ligne : factureLignes) {
                ligne.setFacture(this);
            }
        }
    }
}
