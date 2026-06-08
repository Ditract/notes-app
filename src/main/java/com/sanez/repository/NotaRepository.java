package com.sanez.repository;

import com.sanez.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByUsuarioId(Long usuarioId);
    long countByUsuarioId(Long usuarioId);
    List<Nota> findByUsuarioIdAndTituloContainingIgnoreCase(Long usuarioId, String keyword);
    List<Nota> findByUsuarioIdAndContenidoContainingIgnoreCase(Long usuarioId, String keyword);
    List<Nota> findByUsuarioIdAndCategoria(Long usuarioId, String categoria);
    List<Nota> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
    long countByUsuarioIdAndCategoria(Long usuarioId, String categoria);
}
