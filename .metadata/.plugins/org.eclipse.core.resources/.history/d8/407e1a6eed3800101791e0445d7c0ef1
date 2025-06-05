package org.ms.facture_service.repository;
import org.ms.facture_service.entities.FactureLigne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import java.util.Collection;
@RepositoryRestController
public interface FactureLigneRepository extends
        JpaRepository<FactureLigne, Long> {
    Collection<FactureLigne> findByFactureId(Long factureId);
}