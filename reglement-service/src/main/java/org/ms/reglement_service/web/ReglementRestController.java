package org.ms.reglement_service.web;

import jakarta.validation.Valid;
import org.ms.reglement_service.entities.Reglement;
import org.ms.reglement_service.feign.FactureServiceClient;
import org.ms.reglement_service.feign.DeviseServiceClient;
import org.ms.reglement_service.model.Facture;
import org.ms.reglement_service.model.Devise;
import org.ms.reglement_service.repository.ReglementRepository;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

@RefreshScope
//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/reglements")
public class ReglementRestController {
	private final ReglementRepository reglementRepository;
	private final FactureServiceClient factureServiceClient;
	private final DeviseServiceClient deviseServiceClient;
	private static final Logger logger = LoggerFactory.getLogger(ReglementRestController.class);
	private static final List<String> STATUTS_VALIDES = List.of("COMPLET", "PARTIEL", "ANNULE");

	public ReglementRestController(ReglementRepository reglementRepository, FactureServiceClient factureServiceClient,
			DeviseServiceClient deviseServiceClient) {
		this.reglementRepository = reglementRepository;
		this.factureServiceClient = factureServiceClient;
		this.deviseServiceClient = deviseServiceClient;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
	public Map<String, Object> getAllReglements(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String statut,
			@RequestParam(required = false) String modePaiement) {
		Page<Reglement> pageReglements = reglementRepository.findByStatutAndModePaiement(statut, modePaiement,
				PageRequest.of(page, size));
		List<Map<String, Object>> enrichedReglements = pageReglements.getContent().stream()
				.map(this::enrichReglementWithFactureDetails).collect(Collectors.toList());
		Map<String, Object> response = new HashMap<>();
		response.put("reglements", enrichedReglements);
		response.put("totalPages", pageReglements.getTotalPages());
		response.put("totalElements", pageReglements.getTotalElements());
		return response;
	}

	// Récupérer un règlement par son ID

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
	public Map<String, Object> getReglement(@PathVariable Long id) {
		Reglement reglement = reglementRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reglement not found for ID: " + id));
		return enrichReglementWithFactureDetails(reglement);
	}

	// Créer un nouveau règlement

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('ADMIN')")
	public Reglement createReglement(@Valid @RequestBody Reglement reglement) {
		validateReglement(reglement);
		Reglement saved = reglementRepository.save(reglement);
		updateFactureStatus(saved.getFactureId());
		return saved;
	}

	// Mettre à jour un règlement existant
	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Reglement updateReglement(@PathVariable Long id, @Valid @RequestBody Reglement reglement) {
		Reglement existing = reglementRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Règlement non trouvé"));
		validateReglement(reglement);
		// Met à jour les champs
		existing.setMontant(reglement.getMontant());
		existing.setDateReglement(reglement.getDateReglement());
		existing.setModePaiement(reglement.getModePaiement());
		existing.setReference(reglement.getReference());
		existing.setStatut(reglement.getStatut());
		existing.setFactureId(reglement.getFactureId());
		Reglement updated = reglementRepository.save(existing);
		updateFactureStatus(updated.getFactureId());
		return updated;
	}

	// Supprimer un règlement

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ADMIN')")
	public void deleteReglement(@PathVariable Long id) {
		Reglement reglement = reglementRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Règlement non trouvé"));
		reglementRepository.deleteById(id);
		updateFactureStatus(reglement.getFactureId());
	}

	// Récupérer les règlements d’une facture
	@GetMapping("/facture/{factureId}")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
	public List<Reglement> getReglementsByFacture(@PathVariable Long factureId) {
		return reglementRepository.findByFactureId(factureId);
	}

	@GetMapping("/client/{clientId}")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
	public Map<String, Object> getReglementsByClient(@PathVariable Long clientId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			@RequestParam(required = false) String statut, @RequestParam(required = false) String modePaiement) {

		List<Long> factureIds;
		try {
			factureIds = factureServiceClient.getFactureIdsByClient(clientId);
		} catch (FeignException e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Facture service unavailable: " + e.getMessage());
		}

		if (factureIds.isEmpty()) {
			// Pas de factures, donc pas de règlements
			Map<String, Object> emptyResponse = new HashMap<>();
			emptyResponse.put("reglements", Collections.emptyList());
			emptyResponse.put("totalPages", 0);
			emptyResponse.put("totalElements", 0);
			return emptyResponse;
		}

		// Pour gérer les filtres optionnels, on remplace null par "" pour utiliser
		// ContainingIgnoreCase
		String statutFilter = (statut != null) ? statut : "";
		String modePaiementFilter = (modePaiement != null) ? modePaiement : "";

		Page<Reglement> pageReglements = reglementRepository
				.findByFactureIdInAndStatutContainingIgnoreCaseAndModePaiementContainingIgnoreCase(factureIds,
						statutFilter, modePaiementFilter, PageRequest.of(page, size));

		List<Map<String, Object>> enrichedReglements = pageReglements.getContent().stream()
				.map(this::enrichReglementWithFactureDetails).collect(Collectors.toList());

		Map<String, Object> response = new HashMap<>();
		response.put("reglements", enrichedReglements);
		response.put("totalPages", pageReglements.getTotalPages());
		response.put("totalElements", pageReglements.getTotalElements());

		return response;
	}

	// --- Validation et mise à jour du statut de la facture ---

	private void validateReglement(Reglement reglement) {
		if (reglement.getFactureId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Facture ID is required");
		}
		if (reglement.getMontant() == null || reglement.getMontant() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant must be positive and not null");
		}

		Facture facture;
		try {
			facture = factureServiceClient.findFactureById(reglement.getFactureId());
		} catch (FeignException e) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Facture service unavailable: " + e.getMessage());
		}
		if (facture == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Facture not found for ID: " + reglement.getFactureId());
		}

		if (reglement.getDateReglement() == null) {
			reglement.setDateReglement(new Date());
		}

		if (reglement.getStatut() == null || !STATUTS_VALIDES.contains(reglement.getStatut())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid or missing statut: must be COMPLET, PARTIEL, or ANNULE");
		}

		if (reglement.getModePaiement() == null) {
			reglement.setModePaiement("MAD");
		} else {
			try {
				Devise devise = deviseServiceClient.findDeviseByCode(reglement.getModePaiement());
				if (devise == null || devise.getCode() == null) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Invalid currency in modePaiement: " + reglement.getModePaiement());
				}
			} catch (FeignException e) {
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
						"Devise service unavailable: " + e.getMessage());
			}
		}

		if (reglement.getReference() == null) {
			reglement.setReference("PAY-" + System.currentTimeMillis());
		}
	}

	private void updateFactureStatus(Long factureId) {
		try {
			Facture facture = factureServiceClient.findFactureById(factureId);
			if (facture == null || facture.getTotal() == null || facture.getTotal() <= 0) {
				logger.warn("Facture introuvable ou total invalide pour factureId={}", factureId);
				return;
			}
			List<Reglement> reglements = reglementRepository.findByFactureId(factureId);
			double totalPaid = reglements.stream().filter(r -> !"ANNULE".equals(r.getStatut())).mapToDouble(r -> {
				if (!"MAD".equals(r.getModePaiement())) {
					Devise devise = deviseServiceClient.findDeviseByCode(r.getModePaiement());
					if (devise == null) {
						logger.warn("Devise inconnue pour code {}", r.getModePaiement());
						return 0.0;
					}
					return r.getMontant() * devise.getTauxChange();
				}
				return r.getMontant();
			}).sum();

			String newStatus;
			if (totalPaid == 0)
				newStatus = "NON_PAYEE";
			else if (totalPaid < facture.getTotal())
				newStatus = "PARTIELLEMENT_PAYEE";
			else
				newStatus = "PAYEE";

			factureServiceClient.updateFactureMontantPaye(factureId, totalPaid);
			factureServiceClient.updateFactureStatus(factureId, newStatus);

			logger.info("Mise à jour du statut facture {} : montant payé = {}, statut = {}", factureId, totalPaid,
					newStatus);
		} catch (Exception e) {
			logger.error("Erreur lors de la mise à jour du statut de la facture {} : {}", factureId, e.getMessage(), e);
		}
	}

	private Map<String, Object> enrichReglementWithFactureDetails(Reglement reglement) {
		Map<String, Object> reglementMap = new HashMap<>();
		reglementMap.put("id", reglement.getId());
		reglementMap.put("factureId", reglement.getFactureId());
		reglementMap.put("montant", reglement.getMontant());
		reglementMap.put("dateReglement", reglement.getDateReglement());
		reglementMap.put("modePaiement", reglement.getModePaiement());
		reglementMap.put("reference", reglement.getReference());
		reglementMap.put("statut", reglement.getStatut());

		try {
			Facture facture = factureServiceClient.findFactureById(reglement.getFactureId());
			if (facture != null) {
				Map<String, Object> factureDetails = new HashMap<>();
				factureDetails.put("total", facture.getTotal());
				factureDetails.put("status", facture.getStatus());
				if (facture.getClient() != null) {
					factureDetails.put("clientName", facture.getClient().getName());
					factureDetails.put("clientId", facture.getClient().getId());
				}
				reglementMap.put("factureDetails", factureDetails);
			} else {
				reglementMap.put("factureDetails", "Facture not found");
			}
		} catch (FeignException e) {
			logger.error("Erreur lors de la récupération de la facture {} : {}", reglement.getFactureId(),
					e.getMessage());
			reglementMap.put("factureDetails", "Facture service unavailable");
		}
		return reglementMap;
	}

	@GetMapping("/facture/{factureId}/sum")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
	public Double sumByFactureId(@PathVariable Long factureId) {
		return reglementRepository.findByFactureId(factureId).stream().filter(r -> !"ANNULE".equals(r.getStatut()))
				.mapToDouble(Reglement::getMontant).sum();
	}

}