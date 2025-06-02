package org.ms.produit_service.repository;

import java.util.List;
import java.util.Optional;

import org.ms.produit_service.entities.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByName(String name);
    List<Categorie> findByNameContainingIgnoreCase(String name);

}