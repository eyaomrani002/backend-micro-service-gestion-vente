package org.ms.facture_service.web;

import org.ms.facture_service.entities.Facture;
import org.ms.facture_service.entities.FactureLigne;
import org.ms.facture_service.feign.ClientServiceClient;
import org.ms.facture_service.feign.ProduitServiceClient;
import org.ms.facture_service.model.Client;
import org.ms.facture_service.model.Produit;
import org.ms.facture_service.repository.FactureLigneRepository;
import org.ms.facture_service.repository.FactureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/factures")
public class FactureRestController {
    private final FactureRepository factureRepository;
    private final FactureLigneRepository factureLigneRepository;
    private final ClientServiceClient clientServiceClient;
    private final ProduitServiceClient produitServiceClient;

    public FactureRestController(FactureRepository factureRepository,
                                 FactureLigneRepository factureLigneRepository,
                                 ClientServiceClient clientServiceClient,
                                 ProduitServiceClient produitServiceClient) {
        this.factureRepository = factureRepository;
        this.factureLigneRepository = factureLigneRepository;
        this.clientServiceClient = clientServiceClient;
        this.produitServiceClient = produitServiceClient;
    }

    // --- Helper Method to Enrich Facture ---
    private Facture enrichFacture(Facture facture) {
        // Fetch client details
        if (facture.getClientID() != null) {
            try {
                Client client = clientServiceClient.findClientById(facture.getClientID());
                facture.setClient(client);
            } catch (Exception e) {
                facture.setClient(new Client(facture.getClientID(), "Client indisponible", "N/A", "N/A"));
            }
        }

        // Fetch product details for each facture ligne
        if (facture.getFactureLignes() != null) {
            for (FactureLigne ligne : facture.getFactureLignes()) {
                try {
                    Produit produit = produitServiceClient.getProduitById(ligne.getProduitID());
                    ligne.setProduit(produit);
                } catch (Exception e) {
                    ligne.setProduit(new Produit(ligne.getProduitID(), "Produit indisponible", 0.0, 0, null, null));
                }
            }
        }
        return facture;
    }

    // --- CRUD ---

    @PostMapping
    @Transactional
    public ResponseEntity<Facture> createFacture(@Valid @RequestBody Facture facture) {
        // Validate client
        Client client = clientServiceClient.findClientById(facture.getClientID());
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client non trouvé pour ID: " + facture.getClientID());
        }
        facture.setClient(client);

        // Validate and update stock for each facture ligne
        if (facture.getFactureLignes() == null || facture.getFactureLignes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La facture doit contenir au moins une ligne de produit");
        }

        for (FactureLigne ligne : facture.getFactureLignes()) {
            Produit produit = produitServiceClient.getProduitById(ligne.getProduitID());
            if (produit == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit non trouvé pour ID: " + ligne.getProduitID());
            }
            long quantityLong = ligne.getQuantity();
            if (quantityLong <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantité doit être positive pour le produit ID: " + ligne.getProduitID());
            }
            if (quantityLong > Integer.MAX_VALUE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantité dépasse la limite maximale pour le produit ID: " + ligne.getProduitID());
            }
            int quantity = (int) quantityLong;
            if (produit.getQuantity() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuffisant pour " + produit.getName() + ": disponible " + produit.getQuantity() + ", requis " + quantity);
            }
            // Decrease stock
            ResponseEntity<Produit> response = produitServiceClient.decreaseStock(ligne.getProduitID(), quantity);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de la diminution du stock pour le produit ID: " + ligne.getProduitID());
            }
            ligne.setPrice(produit.getPrice());
            ligne.setFacture(facture);
        }

        // Set creation date
        facture.setDateFacture(new Date());
        // Save facture
        Facture savedFacture = factureRepository.save(facture);
        // Save facture lignes
        for (FactureLigne ligne : facture.getFactureLignes()) {
            factureLigneRepository.save(ligne);
        }
        return new ResponseEntity<>(enrichFacture(savedFacture), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facture> getFactureById(@PathVariable Long id) {
        Optional<Facture> factureOpt = factureRepository.findById(id);
        return factureOpt.map(facture -> ResponseEntity.ok(enrichFacture(facture)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Facture> updateFacture(@PathVariable Long id, @RequestBody Facture facture) {
        if (!factureRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        facture.setId(id);
        if (facture.getFactureLignes() != null) {
            for (FactureLigne ligne : facture.getFactureLignes()) {
                ligne.setFacture(facture);
            }
        }
        Facture updatedFacture = factureRepository.save(facture);
        return ResponseEntity.ok(enrichFacture(updatedFacture));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        if (!factureRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        factureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Facture> getAllFactures(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Page<Facture> facturePage = factureRepository.findAll(PageRequest.of(page, size));
        return facturePage.getContent().stream()
                .map(this::enrichFacture)
                .collect(Collectors.toList());
    }

    // --- Client-Service Endpoints ---

    @GetMapping("/client/{clientId}")
    public List<Facture> findFacturesByClient(@PathVariable Long clientId,
                                             @RequestParam(required = false) String statut) {
        List<Facture> factures = factureRepository.findByClientID(clientId);
        if (statut != null) {
            factures = factures.stream().filter(f -> statut.equals(f.getStatus())).collect(Collectors.toList());
        }
        return factures.stream().map(this::enrichFacture).collect(Collectors.toList());
    }

    @GetMapping("/client/{clientId}/ids")
    public List<Long> getFactureIdsByClient(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId).stream()
                .map(Facture::getId)
                .collect(Collectors.toList());
    }

    @GetMapping("/client/{clientId}/total")
    public Double getChiffreAffairesByClient(@PathVariable Long clientId,
                                             @RequestParam(required = false) Integer annee) {
        return factureRepository.findByClientID(clientId).stream().filter(
                f -> annee == null || (f.getDateFacture() != null && (f.getDateFacture().getYear() + 1900) == annee))
                .mapToDouble(Facture::getTotal).sum();
    }

    @GetMapping("/client/{clientId}/reste-a-payer")
    public Double getResteAPayerByClient(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId).stream().filter(f -> !"PAYEE".equals(f.getStatus()))
                .mapToDouble(Facture::getResteAPayer).sum();
    }

    @GetMapping("/lignes/client/{clientId}/produits")
    public List<Map<String, Object>> getProduitsSollicitesByClient(@PathVariable Long clientId,
                                                                   @RequestParam(defaultValue = "5") int limit) {
        List<Object[]> results = factureLigneRepository.findProduitQuantitesByClientId(clientId);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("produitId", row[0]);
            map.put("quantite", ((Number) row[1]).longValue());
            try {
                Produit produit = produitServiceClient.getProduitById((Long) row[0]);
                map.put("produitNom", produit.getName());
            } catch (Exception e) {
                map.put("produitNom", "Produit indisponible");
            }
            return map;
        }).limit(limit).collect(Collectors.toList());
    }

    // --- Produit-Service Endpoint ---

    @GetMapping("/lignes/produit/{produitId}/quantite")
    public Long getQuantiteVendueByProduit(@PathVariable Long produitId,
                                           @RequestParam(required = false) Integer annee) {
        List<FactureLigne> lignes = factureLigneRepository.findAll().stream()
                .filter(l -> l.getProduitID().equals(produitId))
                .filter(l -> annee == null || (l.getFacture().getDateFacture() != null
                        && (l.getFacture().getDateFacture().getYear() + 1900) == annee))
                .collect(Collectors.toList());
        return lignes.stream().mapToLong(FactureLigne::getQuantity).sum();
    }

    // --- Statistical Endpoints ---

    @GetMapping("/stats/chiffre-affaires/{clientId}")
    public Double getChiffreAffaires(@PathVariable Long clientId,
                                     @RequestParam(required = false) Integer annee) {
        return factureRepository.findByClientID(clientId).stream().filter(
                f -> annee == null || (f.getDateFacture() != null && (f.getDateFacture().getYear() + 1900) == annee))
                .mapToDouble(Facture::getTotal).sum();
    }

    @GetMapping("/stats/reste-a-payer/{clientId}")
    public Double getResteAPayer(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId).stream().filter(f -> !"PAYEE".equals(f.getStatus()))
                .mapToDouble(Facture::getResteAPayer).sum();
    }

    @GetMapping("/stats/reglees/{clientId}")
    public List<Facture> getFacturesReglees(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId).stream()
                .filter(f -> "PAYEE".equals(f.getStatus()))
                .map(this::enrichFacture)
                .collect(Collectors.toList());
    }

    @GetMapping("/stats/non-reglees/{clientId}")
    public List<Facture> getFacturesNonReglees(@PathVariable Long clientId) {
        return factureRepository.findByClientID(clientId).stream()
                .filter(f -> !"PAYEE".equals(f.getStatus()))
                .map(this::enrichFacture)
                .collect(Collectors.toList());
    }

    @GetMapping("/stats/non-reglees")
    public List<Facture> getAllFacturesNonReglees() {
        return factureRepository.findAll().stream()
                .filter(f -> !"PAYEE".equals(f.getStatus()))
                .map(this::enrichFacture)
                .collect(Collectors.toList());
    }

    @GetMapping("/stats/reglees")
    public List<Facture> getAllFacturesReglees() {
        return factureRepository.findAll().stream()
                .filter(f -> "PAYEE".equals(f.getStatus()))
                .map(this::enrichFacture)
                .collect(Collectors.toList());
    }

    @GetMapping("/stats/produits-top/{clientId}")
    public List<Map<String, Object>> getProduitsTopParClient(@PathVariable Long clientId,
                                                             @RequestParam(defaultValue = "5") int limit) {
        List<Object[]> results = factureLigneRepository.findProduitQuantitesByClientId(clientId);
        return results.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("produitId", row[0]);
            map.put("quantite", ((Number) row[1]).longValue());
            try {
                Produit produit = produitServiceClient.getProduitById((Long) row[0]);
                map.put("produitNom", produit.getName());
            } catch (Exception e) {
                map.put("produitNom", "Produit indisponible");
            }
            return map;
        }).limit(limit).collect(Collectors.toList());
    }

    @GetMapping("/stats/clients-fideles")
    public List<Map<String, Object>> getClientsFideles(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Integer annee) {
        List<Facture> factures = factureRepository.findAll();
        if (annee != null) {
            factures = factures.stream()
                    .filter(f -> f.getDateFacture() != null && (f.getDateFacture().getYear() + 1900) == annee)
                    .collect(Collectors.toList());
        }
        Map<Long, Double> caParClient = factures.stream()
                .collect(Collectors.groupingBy(Facture::getClientID, Collectors.summingDouble(Facture::getTotal)));
        return caParClient.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("clientId", entry.getKey());
                    map.put("chiffreAffaires", entry.getValue());
                    try {
                        Client client = clientServiceClient.findClientById(entry.getKey());
                        map.put("clientNom", client.getName());
                    } catch (Exception e) {
                        map.put("clientNom", "Client indisponible");
                    }
                    return map;
                }).collect(Collectors.toList());
    }
    @GetMapping("/full-facture/{id}")
    public Facture getFullFacture(@PathVariable Long id) {
        Facture facture = factureRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture not found for ID: " + id));
        return enrichFacture(facture);
    }

    // --- Reglement-Service Endpoints ---

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateFactureStatus(@PathVariable Long id, @RequestParam String status) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture not found for ID: " + id));
        facture.setStatus(status);
        factureRepository.save(facture);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/total")
    public Double getFactureTotal(@PathVariable Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture not found for ID: " + id));
        return facture.getTotal();
    }

    @PutMapping("/{id}/montant-paye")
    public ResponseEntity<Void> updateFactureMontantPaye(@PathVariable Long id, @RequestParam Double montantPaye) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture not found for ID: " + id));
        facture.setMontantPaye(montantPaye);
        factureRepository.save(facture);
        return ResponseEntity.ok().build();
    }

    // --- Dashboard Endpoints ---

    @GetMapping("/sales/summary")
    public Map<String, Object> getSalesSummary() {
        List<Facture> factures = factureRepository.findAll();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGlobal", factures.stream().mapToDouble(Facture::getTotal).sum());
        Map<Integer, Double> totalYearly = factures.stream()
                .filter(f -> f.getDateFacture() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getDateFacture().getYear() + 1900,
                        Collectors.summingDouble(Facture::getTotal)));
        summary.put("totalYearly", totalYearly);
        return summary;
    }

    @GetMapping("/invoices/summary")
    public Map<String, Object> getInvoiceSummary() {
        List<Facture> factures = factureRepository.findAll();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGenerated", factures.size());
        summary.put("paidCount", factures.stream().filter(f -> "PAYEE".equals(f.getStatus())).count());
        summary.put("pendingCount", factures.stream().filter(f -> !"PAYEE".equals(f.getStatus())).count());
        return summary;
    }

    @GetMapping("/invoices")
    public List<Map<String, Object>> getInvoices() {
        return factureRepository.findAll().stream().map(facture -> {
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("id", facture.getId());
            invoice.put("customerId", facture.getClientID());
            try {
                Client client = clientServiceClient.findClientById(facture.getClientID());
                invoice.put("customerName", client.getName());
            } catch (Exception e) {
                invoice.put("customerName", "Client indisponible");
            }
            invoice.put("amount", facture.getTotal());
            invoice.put("status", facture.getStatus());
            invoice.put("date", facture.getDateFacture());
            return invoice;
        }).collect(Collectors.toList());
    }

    @GetMapping("/sales/trends")
    public List<Map<String, Object>> getSalesTrends() {
        return factureRepository.findAll().stream()
                .filter(f -> f.getDateFacture() != null)
                .collect(Collectors.groupingBy(
                        f -> {
                            LocalDate date = ((java.sql.Date) f.getDateFacture()).toLocalDate();
                            return date.getYear() + "-" + date.getMonthValue();
                        },
                        Collectors.summingDouble(Facture::getTotal)))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> trend = new HashMap<>();
                    String[] parts = entry.getKey().split("-");
                    trend.put("year", Integer.parseInt(parts[0]));
                    trend.put("month", String.format("%02d", Integer.parseInt(parts[1])));
                    trend.put("total", entry.getValue());
                    return trend;
                })
                .sorted((a, b) -> {
                    int yearCompare = ((Integer) a.get("year")).compareTo((Integer) b.get("year"));
                    if (yearCompare != 0) return yearCompare;
                    return ((String) a.get("month")).compareTo((String) b.get("month"));
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/invoices/payment-rate")
    public Map<String, Object> getPaymentRate() {
        List<Facture> factures = factureRepository.findAll();
        long totalInvoices = factures.size();
        long paidInvoices = factures.stream().filter(f -> "PAYEE".equals(f.getStatus())).count();
        Map<String, Object> paymentRate = new HashMap<>();
        paymentRate.put("paidPercentage", totalInvoices > 0 ? (paidInvoices * 100.0 / totalInvoices) : 0.0);
        paymentRate.put("pendingPercentage", totalInvoices > 0 ? ((totalInvoices - paidInvoices) * 100.0 / totalInvoices) : 0.0);
        return paymentRate;
    }
    
 // --- Produit le plus vendu (Dashboard) ---
    @GetMapping("/stats/produit-top")
    public Map<String, Object> getProduitLePlusVendu(@RequestParam(defaultValue = "1") int limit) {
        List<FactureLigne> lignes = factureLigneRepository.findAll();
        // Regrouper par produitId et sommer les quantités
        Map<Long, Long> quantitesParProduit = lignes.stream()
            .collect(Collectors.groupingBy(
                FactureLigne::getProduitID,
                Collectors.summingLong(FactureLigne::getQuantity)
            ));
        // Trier par quantité décroissante et prendre les "limit" premiers
        List<Map.Entry<Long, Long>> topProduits = quantitesParProduit.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());

        // Construire la réponse enrichie avec le nom du produit
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> produits = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : topProduits) {
            Map<String, Object> produitMap = new HashMap<>();
            produitMap.put("produitId", entry.getKey());
            produitMap.put("quantiteVendue", entry.getValue());
            try {
                Produit produit = produitServiceClient.getProduitById(entry.getKey());
                produitMap.put("produitNom", produit.getName());
            } catch (Exception e) {
                produitMap.put("produitNom", "Produit indisponible");
            }
            produits.add(produitMap);
        }
        result.put("topProduits", produits);
        return result;
    }
    @GetMapping("/stats/factures-count")
    public Map<String, Object> getFacturesStats() {
        List<Facture> factures = factureRepository.findAll();
        int total = factures.size();
        int reglees = (int) factures.stream().filter(f -> "PAYEE".equals(f.getStatus())).count();
        int nonReglees = total - reglees;
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("reglees", reglees);
        stats.put("nonReglees", nonReglees);
        return stats;
    }
}