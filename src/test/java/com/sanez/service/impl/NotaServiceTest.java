package com.sanez.service.impl;

import com.sanez.config.CategoriaConstantes;
import com.sanez.dto.nota.EstadisticasResponseDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.model.Nota;
import com.sanez.model.Perfil;
import com.sanez.model.Usuario;
import com.sanez.repository.NotaRepository;
import com.sanez.repository.PerfilRepository;
import com.sanez.repository.UsuarioRepository;
import com.sanez.security.service.CustomUserDetails;
import com.sanez.service.PerfilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotaService - Tests Unitarios")
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private PerfilService perfilService;

    @InjectMocks
    private NotaServiceImpl notaService;

    private Usuario usuario;
    private CustomUserDetails userDetails;
    private static final Long USUARIO_ID = 1L;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setEmail("test@example.com");

        userDetails = new CustomUserDetails(USUARIO_ID, "test@example.com", "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ==================== TESTS DE BUSCAR NOTAS ====================

    @Test
    @DisplayName("buscarNotas - Keyword matchea en título")
    void buscarNotas_matcheaEnTitulo() {
        // Arrange
        String keyword = "reunion";
        Nota nota1 = crearNota(1L, "Reunión de equipo", "Contenido cualquiera");
        when(notaRepository.findByUsuarioIdAndTituloContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of(nota1));
        when(notaRepository.findByUsuarioIdAndContenidoContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of());

        // Act
        List<NotaResponseDTO> resultado = notaService.buscarNotas(keyword);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Reunión de equipo", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("buscarNotas - Keyword matchea en contenido")
    void buscarNotas_matcheaEnContenido() {
        // Arrange
        String keyword = "sprint";
        Nota nota1 = crearNota(1L, "Planificación", "Discutir avances del sprint actual");
        when(notaRepository.findByUsuarioIdAndTituloContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of());
        when(notaRepository.findByUsuarioIdAndContenidoContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of(nota1));

        // Act
        List<NotaResponseDTO> resultado = notaService.buscarNotas(keyword);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Planificación", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("buscarNotas - Keyword matchea en ambos sin duplicados")
    void buscarNotas_matcheaEnAmbos_sinDuplicados() {
        // Arrange
        String keyword = "proyecto";
        Nota nota1 = crearNota(1L, "Nuevo proyecto", "Ideas para el nuevo proyecto");
        when(notaRepository.findByUsuarioIdAndTituloContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of(nota1));
        when(notaRepository.findByUsuarioIdAndContenidoContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of(nota1));

        // Act
        List<NotaResponseDTO> resultado = notaService.buscarNotas(keyword);

        // Assert
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("buscarNotas - Sin resultados")
    void buscarNotas_sinResultados() {
        // Arrange
        String keyword = "inexistente";
        when(notaRepository.findByUsuarioIdAndTituloContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of());
        when(notaRepository.findByUsuarioIdAndContenidoContainingIgnoreCase(USUARIO_ID, keyword))
                .thenReturn(List.of());

        // Act
        List<NotaResponseDTO> resultado = notaService.buscarNotas(keyword);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS DE NOTAS RECIENTES ====================

    @Test
    @DisplayName("obtenerNotasRecientes - Con limit válido")
    void obtenerNotasRecientes_conLimitValido() {
        // Arrange
        int limit = 3;
        List<Nota> notas = List.of(
                crearNota(1L, "Nota 1", "Contenido 1"),
                crearNota(2L, "Nota 2", "Contenido 2"),
                crearNota(3L, "Nota 3", "Contenido 3"),
                crearNota(4L, "Nota 4", "Contenido 4"),
                crearNota(5L, "Nota 5", "Contenido 5")
        );
        when(notaRepository.findByUsuarioIdOrderByCreatedAtDesc(USUARIO_ID)).thenReturn(notas);

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasRecientes(limit);

        // Assert
        assertEquals(3, resultado.size());
    }

    @Test
    @DisplayName("obtenerNotasRecientes - Limit fuera de rango se ajusta a 50")
    void obtenerNotasRecientes_limitFueraDeRango_seAjustaA50() {
        // Arrange
        int limit = 100;
        List<Nota> notas = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            notas.add(crearNota((long) i, "Nota " + i, "Contenido " + i));
        }
        when(notaRepository.findByUsuarioIdOrderByCreatedAtDesc(USUARIO_ID)).thenReturn(notas);

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasRecientes(limit);

        // Assert
        assertEquals(50, resultado.size());
    }

    // ==================== TESTS DE NOTAS POR CATEGORÍA ====================

    @Test
    @DisplayName("obtenerNotasPorCategoria - Con categoría existente")
    void obtenerNotasPorCategoria_conCategoriaExistente() {
        // Arrange
        String categoria = CategoriaConstantes.TRABAJO;
        Nota nota1 = crearNotaConCategoria(1L, "Tarea 1", "Contenido", categoria);
        Nota nota2 = crearNotaConCategoria(2L, "Tarea 2", "Contenido", categoria);
        when(notaRepository.findByUsuarioIdAndCategoria(USUARIO_ID, categoria))
                .thenReturn(List.of(nota1, nota2));

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasPorCategoria(categoria);

        // Assert
        assertEquals(2, resultado.size());
        assertEquals(categoria, resultado.get(0).getCategoria());
    }

    @Test
    @DisplayName("obtenerNotasPorCategoria - Con categoría inexistente retorna lista vacía")
    void obtenerNotasPorCategoria_conCategoriaInexistente_listaVacia() {
        // Arrange
        String categoria = "INEXISTENTE";
        when(notaRepository.findByUsuarioIdAndCategoria(USUARIO_ID, categoria))
                .thenReturn(List.of());

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasPorCategoria(categoria);

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS DE ESTADÍSTICAS ====================

    @Test
    @DisplayName("obtenerEstadisticas - Con datos")
    void obtenerEstadisticas_conDatos() {
        // Arrange
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setNotasFavoritas(new ArrayList<>(List.of(1L, 2L)));

        Nota notaReciente = crearNotaConCategoria(1L, "Nota reciente", "Contenido", CategoriaConstantes.TRABAJO);
        notaReciente.setCreatedAt(LocalDateTime.now().minusHours(1));
        Nota notaAntigua = crearNotaConCategoria(2L, "Nota antigua", "Contenido", CategoriaConstantes.PERSONAL);
        notaAntigua.setCreatedAt(LocalDateTime.now().minusDays(30));

        when(notaRepository.countByUsuarioId(USUARIO_ID)).thenReturn(5L);
        when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, CategoriaConstantes.TRABAJO)).thenReturn(2L);
        when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, CategoriaConstantes.PERSONAL)).thenReturn(1L);
        when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, CategoriaConstantes.IDEAS)).thenReturn(1L);
        when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, CategoriaConstantes.REUNIONES)).thenReturn(1L);
        when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, CategoriaConstantes.TAREAS)).thenReturn(0L);
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));
        when(notaRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(notaReciente, notaAntigua));
        when(notaRepository.findByUsuarioIdOrderByCreatedAtDesc(USUARIO_ID)).thenReturn(List.of(notaReciente, notaAntigua));

        // Act
        EstadisticasResponseDTO resultado = notaService.obtenerEstadisticas();

        // Assert
        assertEquals(5L, resultado.getTotalNotas());
        assertEquals(5, resultado.getNotasPorCategoria().size());
        assertEquals(2L, resultado.getNotasPorCategoria().get(CategoriaConstantes.TRABAJO));
        assertEquals(2L, resultado.getTotalFavoritas());
        assertNotNull(resultado.getNotaMasReciente());
        assertNotNull(resultado.getNotaMasAntigua());
    }

    @Test
    @DisplayName("obtenerEstadisticas - Ignora IDs de favoritas huérfanos")
    void obtenerEstadisticas_ignoraFavoritasHuerfanas() {
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setNotasFavoritas(new ArrayList<>(List.of(1L, 2L, 99L)));

        Nota nota1 = crearNota(1L, "Favorita 1", "Contenido");
        Nota nota2 = crearNota(2L, "Favorita 2", "Contenido");

        when(notaRepository.countByUsuarioId(USUARIO_ID)).thenReturn(2L);
        for (String cat : CategoriaConstantes.TODAS) {
            when(notaRepository.countByUsuarioIdAndCategoria(USUARIO_ID, cat)).thenReturn(0L);
        }
        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));
        when(notaRepository.findAllById(List.of(1L, 2L, 99L))).thenReturn(List.of(nota1, nota2));
        when(notaRepository.findByUsuarioIdOrderByCreatedAtDesc(USUARIO_ID)).thenReturn(List.of(nota1, nota2));

        EstadisticasResponseDTO resultado = notaService.obtenerEstadisticas();

        assertEquals(2L, resultado.getTotalFavoritas());
    }

    @Test
    @DisplayName("eliminarNota - Remueve la nota de favoritas antes de borrarla")
    void eliminarNota_remueveDeFavoritas() {
        Nota nota = crearNota(1L, "Nota a eliminar", "Contenido");
        when(notaRepository.findById(1L)).thenReturn(Optional.of(nota));

        notaService.eliminarNota(1L);

        verify(perfilService).removerNotaFavorita(1L);
        verify(notaRepository).delete(nota);
    }

    // ==================== TESTS DE NOTAS FAVORITAS ====================

    @Test
    @DisplayName("obtenerNotasFavoritas - Con favoritas")
    void obtenerNotasFavoritas_conFavoritas() {
        // Arrange
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setUsuario(usuario);
        perfil.setNotasFavoritas(new ArrayList<>(List.of(1L, 2L)));

        Nota nota1 = crearNota(1L, "Favorita 1", "Contenido");
        nota1.setUsuario(usuario);
        Nota nota2 = crearNota(2L, "Favorita 2", "Contenido");
        nota2.setUsuario(usuario);

        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));
        when(notaRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(nota1, nota2));

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasFavoritas();

        // Assert
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("obtenerNotasFavoritas - Sin favoritas retorna lista vacía")
    void obtenerNotasFavoritas_sinFavoritas_listaVacia() {
        // Arrange
        Perfil perfil = new Perfil();
        perfil.setId(1L);
        perfil.setUsuario(usuario);
        perfil.setNotasFavoritas(new ArrayList<>());

        when(perfilRepository.findByUsuarioId(USUARIO_ID)).thenReturn(Optional.of(perfil));

        // Act
        List<NotaResponseDTO> resultado = notaService.obtenerNotasFavoritas();

        // Assert
        assertTrue(resultado.isEmpty());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Nota crearNota(Long id, String titulo, String contenido) {
        Nota nota = new Nota();
        nota.setId(id);
        nota.setTitulo(titulo);
        nota.setContenido(contenido);
        nota.setUsuario(usuario);
        return nota;
    }

    private Nota crearNotaConCategoria(Long id, String titulo, String contenido, String categoria) {
        Nota nota = crearNota(id, titulo, contenido);
        nota.setCategoria(categoria);
        return nota;
    }
}
