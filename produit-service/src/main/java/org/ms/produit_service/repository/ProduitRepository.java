package org.ms.produit_service.repository;

import java.util.List;

import org.ms.produit_service.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    boolean existsByCategorieId(Long categorieId);
    @Query("SELECT p FROM Produit p JOIN FETCH p.categorie")
    List<Produit> findAllWithCategories();
    List<Produit> findByNameContainingIgnoreCase(String name);


}