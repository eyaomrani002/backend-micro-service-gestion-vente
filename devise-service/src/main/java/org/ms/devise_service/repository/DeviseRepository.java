package org.ms.devise_service.repository;

import org.ms.devise_service.entities.Devise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviseRepository extends JpaRepository<Devise, Long> {
    Devise findByCode(String code);
}
