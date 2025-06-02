package org.ms.authentificationservice.repositories;

import java.util.List;

import org.ms.authentificationservice.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.domain.Example;

@RepositoryRestResource
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
//    @PostAuthorize("hasAuthority('USER')")
    AppRole findByRoleName(String roleName);

    @Override
//    @PostAuthorize("hasAuthority('ADMIN')")
    <S extends AppRole> S save(S entity);

    @Override
//    @PostAuthorize("hasAuthority('ADMIN')")
    void delete(AppRole entity);

    @Override
//    @PostAuthorize("hasAuthority('USER')")
    <S extends AppRole> List<S> findAll(Example<S> example);
}
