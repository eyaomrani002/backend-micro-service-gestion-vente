package org.ms.reglement_service.model;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Facture {
    private Long id;
    private Date dateFacture;
    private Double total;
    private String status;
    private Client client;
    private List<FactureLigne> factureLignes;
    private Double montantPaye;  // <-- Nouveau champ

    @Data
    public static class Client {
        private Long id;
        private String name;
    }

    @Data
    public static class FactureLigne {
        private Produit produit;
        private Integer quantity;
        private Double price;
    }

    @Data
    public static class Produit {
        private Long id;
        private String name;
    }
}
