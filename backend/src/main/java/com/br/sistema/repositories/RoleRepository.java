package com.br.sistema.repositories;

import com.br.sistema.entities.Role.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Override
    Page<Role> findAll(Pageable pageable);


}
