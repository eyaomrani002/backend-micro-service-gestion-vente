package org.ms.client_service.web;

import org.ms.client_service.entities.Client;
import org.ms.client_service.feign.DeviseServiceClient;
import org.ms.client_service.feign.FactureServiceClient;
import org.ms.client_service.feign.ProduitServiceClient;
import org.ms.client_service.model.Facture;
import org.ms.client_service.model.Produit;
import org.ms.client_service.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//@CrossOrigin(origins = "http://localhost:4200")
@RefreshScope
@RestController
@RequestMapping("/clients")
public class ClientRestController {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private FactureServiceClient factureServiceClient;
    @Autowired
    private DeviseServiceClient deviseServiceClient;
    @Autowired
    private ProduitServiceClient produitServiceClient;

    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public List<Client> list() {
        return clientRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public Client getOne(@PathVariable("id") Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Client save(@RequestBody Client client) {
        return clientRepository.save(client);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Client update(@PathVariable("id") Long id, @RequestBody Client client) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        client.setId(id);
        return clientRepository.save(client);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        clientRepository.deleteById(id);
    }

    @GetMapping("/{id}/chiffre-affaires")
    public Double getChiffreAffaires(@PathVariable Long id, @RequestParam(required = false) Integer annee) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        return factureServiceClient.getChiffreAffairesByClient(id, annee);
    }

    @GetMapping("/{id}/chiffre-affaires/convert")
    public Double getChiffreAffairesConverti(@PathVariable Long id,
                                            @RequestParam(required = false) Integer annee,
                                            @RequestParam String devise) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        Double ca = factureServiceClient.getChiffreAffairesByClient(id, annee);
        return deviseServiceClient.convertDevise(ca, "MAD", devise);
    }

    @GetMapping("/{id}/reste-a-payer")
    public Double getResteAPayer(@PathVariable Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        return factureServiceClient.getResteAPayerByClient(id);
    }

    @GetMapping("/{id}/factures")
    public List<Facture> getFacturesByStatut(@PathVariable Long id, @RequestParam(required = false) String statut) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        return factureServiceClient.findFacturesByClientAndStatut(id, statut);
    }

    @GetMapping("/fideles")
    public List<Client> getClientsFideles(@RequestParam(defaultValue = "5") int limit) {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .sorted((c1, c2) -> {
                    Double ca1 = factureServiceClient.getChiffreAffairesByClient(c1.getId(), null);
                    Double ca2 = factureServiceClient.getChiffreAffairesByClient(c2.getId(), null);
                    return ca2.compareTo(ca1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/produits-sollicites")
    public List<Map<String, Object>> getProduitsSollicites(@PathVariable Long id, @RequestParam(defaultValue = "5") int limit) {
        if (!clientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Client non trouvé pour l'ID : " + id);
        }
        List<Map<String, Object>> produitsSollicites = factureServiceClient.getProduitsSollicitesByClient(id, limit);
        return produitsSollicites.stream()
                .map(produitMap -> {
                    // Correction du cast ici :
                    Long produitId;
                    Object idObj = produitMap.get("produitId");
                    if (idObj instanceof Integer) {
                        produitId = ((Integer) idObj).longValue();
                    } else if (idObj instanceof Long) {
                        produitId = (Long) idObj;
                    } else {
                        produitId = null;
                    }
                    try {
                        if (produitId != null) {
                            Produit produit = produitServiceClient.getProduitById(produitId);
                            return Map.of(
                                    "produitId", produitId,
                                    "nom", produit.getName(),
                                    "quantite", produitMap.get("quantite")
                            );
                        } else {
                            return produitMap;
                        }
                    } catch (Exception e) {
                        return produitMap; // Retourner la map de base si le produit n'est pas trouvé
                    }
                })
                .collect(Collectors.toList());
    }

}