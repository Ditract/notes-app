package com.sanez.service.impl;

import com.sanez.config.CategoriaConstantes;
import com.sanez.dto.nota.EstadisticasResponseDTO;
import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;
import com.sanez.exception.AccesoNoAutorizadoException;
import com.sanez.exception.OperacionNoPermitidaException;
import com.sanez.exception.RecursoNoEncontradoException;
import com.sanez.mapper.NotaMapper;
import com.sanez.model.Nota;
import com.sanez.model.Perfil;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.PerfilRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.security.service.CustomUserDetails;
import com.sanez.service.NotaService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotaServiceImpl implements NotaService {

    private final NotaRepository notaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    public NotaServiceImpl(NotaRepository notaRepository, UsuarioRepository usuarioRepository, PerfilRepository perfilRepository) {
        this.notaRepository = notaRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
    }

    // Crear nota
    @Override
    @Transactional
    public NotaResponseDTO crearNota(NotaRequestDTO notaRequestDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario con id " + usuarioId + " no encontrado"));

        Nota nota = NotaMapper.toEntity(notaRequestDTO);
        nota.setUsuario(usuario);

        Nota notaGuardada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaGuardada);
    }

    // Obtener notas por usuario (solo lectura)
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasPorUsuario() {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        return notaRepository.findByUsuarioId(usuarioId).stream()
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // Editar nota
    @Override
    @Transactional
    public NotaResponseDTO editarNota(Long notaId, NotaUpdateDTO notaUpdateDTO) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Nota nota = obtenerNotaValidaParaUsuario(notaId, usuarioId);

        NotaMapper.actualizarNotaDesdeDTO(nota, notaUpdateDTO);

        Nota notaActualizada = notaRepository.save(nota);
        return NotaMapper.toResponseDTO(notaActualizada);
    }

    // Eliminar nota
    @Override
    @Transactional
    public void eliminarNota(Long notaId) {
        Long usuarioId = obtenerIdUsuarioAutenticado();
        Nota nota = obtenerNotaValidaParaUsuario(notaId, usuarioId);

        notaRepository.delete(nota);
    }

    // Buscar notas por título y contenido
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> buscarNotas(String keyword) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        List<Nota> notasPorTitulo = notaRepository.findByUsuarioIdAndTituloContainingIgnoreCase(usuarioId, keyword);
        List<Nota> notasPorContenido = notaRepository.findByUsuarioIdAndContenidoContainingIgnoreCase(usuarioId, keyword);

        Set<Nota> notasUnicas = new LinkedHashSet<>();
        notasUnicas.addAll(notasPorTitulo);
        notasUnicas.addAll(notasPorContenido);

        return notasUnicas.stream()
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // Obtener notas recientes
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasRecientes(int limit) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        int limitValido = Math.max(1, Math.min(limit, 50));

        return notaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId).stream()
                .limit(limitValido)
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // Obtener notas por categoría
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasPorCategoria(String categoria) {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        return notaRepository.findByUsuarioIdAndCategoria(usuarioId, categoria).stream()
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // Obtener estadísticas
    @Override
    @Transactional(readOnly = true)
    public EstadisticasResponseDTO obtenerEstadisticas() {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        long totalNotas = notaRepository.countByUsuarioId(usuarioId);

        Map<String, Long> notasPorCategoria = new LinkedHashMap<>();
        for (String cat : CategoriaConstantes.TODAS) {
            long count = notaRepository.countByUsuarioIdAndCategoria(usuarioId, cat);
            notasPorCategoria.put(cat, count);
        }

        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId).orElse(null);
        long totalFavoritas = (perfil != null) ? perfil.getNotasFavoritas().size() : 0;

        List<Nota> notasOrdenadas = notaRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
        LocalDateTime notaMasReciente = notasOrdenadas.isEmpty() ? null : notasOrdenadas.get(0).getCreatedAt();
        LocalDateTime notaMasAntigua = notasOrdenadas.isEmpty() ? null : notasOrdenadas.get(notasOrdenadas.size() - 1).getCreatedAt();

        return new EstadisticasResponseDTO(totalNotas, notasPorCategoria, totalFavoritas, notaMasReciente, notaMasAntigua);
    }

    // Obtener notas favoritas
    @Override
    @Transactional(readOnly = true)
    public List<NotaResponseDTO> obtenerNotasFavoritas() {
        Long usuarioId = obtenerIdUsuarioAutenticado();

        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil no encontrado para el usuario"));

        List<Long> notasFavoritasIds = perfil.getNotasFavoritas();
        if (notasFavoritasIds.isEmpty()) {
            return List.of();
        }

        return notaRepository.findAllById(notasFavoritasIds).stream()
                .filter(nota -> nota.getUsuario().getId().equals(usuarioId))
                .map(NotaMapper::toResponseDTO)
                .toList();
    }

    // ============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================

    // Obtiene el ID del usuario autenticado o lanza 401
    private Long obtenerIdUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new AccesoNoAutorizadoException("Usuario no autenticado");
    }

    // Verifica que la nota exista y pertenezca al usuario
    private Nota obtenerNotaValidaParaUsuario(Long notaId, Long usuarioId) {
        Nota nota = notaRepository.findById(notaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada"));

        if (!nota.getUsuario().getId().equals(usuarioId)) {
            throw new OperacionNoPermitidaException("No tienes permiso para esta nota");
        }

        return nota;
    }
}
