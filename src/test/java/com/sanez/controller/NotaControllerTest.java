package com.sanez.controller;

import com.sanez.config.CategoriaConstantes;
import com.sanez.dto.nota.EstadisticasResponseDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.security.jwt.JwtUtil;
import com.sanez.service.NotaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotaController.class)
@Import(TestSecurityConfig.class)
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@DisplayName("NotaController - Tests de Integración")
class NotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotaService notaService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private final UserDetails testUser = User.builder()
            .username("test@example.com")
            .password("password")
            .authorities(new SimpleGrantedAuthority("ROLE_USER"))
            .build();

    // ==================== TESTS DE BUSCAR NOTAS ====================

    @Test
    @DisplayName("GET /api/notas/buscar - Retorna 200 con datos correctos")
    void buscarNotas_retorna200() throws Exception {
        // Arrange
        String keyword = "reunion";
        NotaResponseDTO nota = crearNotaResponseDTO(1L, "Reunión de equipo", "Contenido", CategoriaConstantes.REUNIONES);
        when(notaService.buscarNotas(keyword)).thenReturn(List.of(nota));

        // Act & Assert
        mockMvc.perform(get("/api/notas/buscar")
                        .param("q", keyword)
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Reunión de equipo"))
                .andExpect(jsonPath("$[0].categoria").value("REUNIONES"));

        verify(notaService, times(1)).buscarNotas(keyword);
    }

    // ==================== TESTS DE NOTAS RECIENTES ====================

    @Test
    @DisplayName("GET /api/notas/recientes - Retorna 200 con limit válido")
    void obtenerNotasRecientes_retorna200() throws Exception {
        // Arrange
        int limit = 3;
        List<NotaResponseDTO> notas = List.of(
                crearNotaResponseDTO(1L, "Nota 1", "Contenido", CategoriaConstantes.TRABAJO),
                crearNotaResponseDTO(2L, "Nota 2", "Contenido", CategoriaConstantes.PERSONAL),
                crearNotaResponseDTO(3L, "Nota 3", "Contenido", CategoriaConstantes.IDEAS)
        );
        when(notaService.obtenerNotasRecientes(limit)).thenReturn(notas);

        // Act & Assert
        mockMvc.perform(get("/api/notas/recientes")
                        .param("limit", String.valueOf(limit))
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(notaService, times(1)).obtenerNotasRecientes(limit);
    }

    @Test
    @DisplayName("GET /api/notas/recientes - Sin limit usa default 10")
    void obtenerNotasRecientes_sinLimit_usaDefault() throws Exception {
        // Arrange
        when(notaService.obtenerNotasRecientes(10)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/notas/recientes")
                        .with(user(testUser)))
                .andExpect(status().isOk());

        verify(notaService, times(1)).obtenerNotasRecientes(10);
    }

    // ==================== TESTS DE NOTAS POR CATEGORÍA ====================

    @Test
    @DisplayName("GET /api/notas/categoria/{categoria} - Retorna 200 con notas filtradas")
    void obtenerNotasPorCategoria_retorna200() throws Exception {
        // Arrange
        String categoria = CategoriaConstantes.TRABAJO;
        List<NotaResponseDTO> notas = List.of(
                crearNotaResponseDTO(1L, "Tarea 1", "Contenido", categoria),
                crearNotaResponseDTO(2L, "Tarea 2", "Contenido", categoria)
        );
        when(notaService.obtenerNotasPorCategoria(categoria)).thenReturn(notas);

        // Act & Assert
        mockMvc.perform(get("/api/notas/categoria/{categoria}", categoria)
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoria").value("TRABAJO"));

        verify(notaService, times(1)).obtenerNotasPorCategoria(categoria);
    }

    // ==================== TESTS DE ESTADÍSTICAS ====================

    @Test
    @DisplayName("GET /api/notas/estadisticas - Retorna 200 con estadísticas")
    void obtenerEstadisticas_retorna200() throws Exception {
        // Arrange
        Map<String, Long> notasPorCategoria = new LinkedHashMap<>();
        notasPorCategoria.put(CategoriaConstantes.TRABAJO, 3L);
        notasPorCategoria.put(CategoriaConstantes.PERSONAL, 2L);
        notasPorCategoria.put(CategoriaConstantes.IDEAS, 1L);
        notasPorCategoria.put(CategoriaConstantes.REUNIONES, 1L);
        notasPorCategoria.put(CategoriaConstantes.TAREAS, 0L);

        EstadisticasResponseDTO estadisticas = new EstadisticasResponseDTO(
                7L,
                notasPorCategoria,
                2L,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusDays(30)
        );
        when(notaService.obtenerEstadisticas()).thenReturn(estadisticas);

        // Act & Assert
        mockMvc.perform(get("/api/notas/estadisticas")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotas").value(7))
                .andExpect(jsonPath("$.totalFavoritas").value(2))
                .andExpect(jsonPath("$.notasPorCategoria.TRABAJO").value(3));

        verify(notaService, times(1)).obtenerEstadisticas();
    }

    // ==================== TESTS DE NOTAS FAVORITAS ====================

    @Test
    @DisplayName("GET /api/notas/favoritas - Retorna 200 con notas favoritas")
    void obtenerNotasFavoritas_retorna200() throws Exception {
        // Arrange
        List<NotaResponseDTO> notas = List.of(
                crearNotaResponseDTO(1L, "Favorita 1", "Contenido", CategoriaConstantes.TRABAJO),
                crearNotaResponseDTO(2L, "Favorita 2", "Contenido", CategoriaConstantes.PERSONAL)
        );
        when(notaService.obtenerNotasFavoritas()).thenReturn(notas);

        // Act & Assert
        mockMvc.perform(get("/api/notas/favoritas")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(notaService, times(1)).obtenerNotasFavoritas();
    }

    @Test
    @DisplayName("GET /api/notas/favoritas - Sin favoritas retorna lista vacía")
    void obtenerNotasFavoritas_sinFavoritas_listaVacia() throws Exception {
        // Arrange
        when(notaService.obtenerNotasFavoritas()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/notas/favoritas")
                        .with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notaService, times(1)).obtenerNotasFavoritas();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private NotaResponseDTO crearNotaResponseDTO(Long id, String titulo, String contenido, String categoria) {
        NotaResponseDTO dto = new NotaResponseDTO();
        dto.setId(id);
        dto.setTitulo(titulo);
        dto.setContenido(contenido);
        dto.setUsuarioId(1L);
        dto.setCategoria(categoria);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
