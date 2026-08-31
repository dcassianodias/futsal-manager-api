package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.UsuarioTime;
import com.futsalmanager.domain.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioTimeRepository extends JpaRepository<UsuarioTime, UUID> {

    List<UsuarioTime> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    List<UsuarioTime> findByTimeIdAndAtivoTrue(UUID timeId);

    List<UsuarioTime> findByTimeId(UUID timeId);

    Optional<UsuarioTime> findByUsuarioIdAndTimeId(UUID usuarioId, UUID timeId);

    boolean existsByUsuarioIdAndTimeIdAndAtivoTrue(UUID usuarioId, UUID timeId);

    boolean existsByUsuarioIdAndTimeIdAndPerfilAndAtivoTrue(UUID usuarioId, UUID timeId, PerfilUsuario perfil);

    long countByTimeIdAndPerfilAndAtivoTrue(UUID timeId, PerfilUsuario perfil);

    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);
}
