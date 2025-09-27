package com.br.sistema.repositories;

import com.br.sistema.entities.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    // Quantos usuários logaram hoje
    @Query("SELECT COUNT(l) FROM LoginAudit l WHERE DATE(l.dataLogin) = CURRENT_DATE")
    long countLoginsHoje();

    // Quantos usuários logaram desde um instante
    long countByDataLoginAfter(LocalDateTime data);
}