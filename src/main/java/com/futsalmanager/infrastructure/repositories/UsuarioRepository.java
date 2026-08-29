package com.futsalmanager.infrastructure.repositories;

import com.futsalmanager.domain.entities.Usuario;
import com.futsalmanager.domain.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    List<Usuario> findByAtivoTrue();

    List<Usuario> findByTimeIdAndAtivoTrue(UUID timeId);

    List<Usuario> findByTimeIdAndPerfilAndAtivoTrue(UUID timeId, PerfilUsuario perfil);

    long countByTimeIdAndPerfilAndAtivoTrue(UUID timeId, PerfilUsuario perfil);

    Optional<Usuario> findByTimeIdAndEmail(UUID timeId, String email);

    Optional<Usuario> findByEmail(String email);

    boolean existsByTimeIdAndEmailAndAtivoTrue(UUID timeId, String email);

    boolean existsByTimeIdAndEmailAndIdNotAndAtivoTrue(UUID timeId, String email, UUID id);
}
