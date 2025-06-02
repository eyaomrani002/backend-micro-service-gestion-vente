package org.ms.reglement_service.repository;

import org.ms.reglement_service.entities.Reglement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.RepositoryRestController;

import java.util.List;

@RepositoryRestController
public interface ReglementRepository extends JpaRepository<Reglement, Long> {
    List<Reglement> findByFactureId(Long factureId);

    @Query("SELECT SUM(r.montant) FROM Reglement r WHERE r.factureId = :factureId AND r.statut != 'ANNULE'")
    Double sumByFactureId(@Param("factureId") Long factureId);

    Page<Reglement> findByFactureIdIn(List<Long> factureIds, Pageable pageable);

    @Query("SELECT r FROM Reglement r WHERE (:statut IS NULL OR r.statut = :statut) AND (:modePaiement IS NULL OR r.modePaiement = :modePaiement)")
    Page<Reglement> findByStatutAndModePaiement(@Param("statut") String statut, @Param("modePaiement") String modePaiement, Pageable pageable);

    Page<Reglement> findByFactureIdInAndStatutContainingIgnoreCaseAndModePaiementContainingIgnoreCase(
            List<Long> factureIds, String statut, String modePaiement, Pageable pageable);
}