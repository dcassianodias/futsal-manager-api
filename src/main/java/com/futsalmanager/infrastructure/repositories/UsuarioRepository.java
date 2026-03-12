package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    List<Usuario> findByTimeId(UUID timeId);

    List<Usuario> findByTimeIdAndPerfilAndAtivoTrue(UUID timeId, PerfilUsuario perfil);
}
