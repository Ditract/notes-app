package com.sanez.controller;

import com.sanez.dto.nota.EstadisticasResponseDTO;
import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;
import com.sanez.service.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Notas", description = "CRUD de notas del usuario autenticado")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/notas")
public class NotaController {

    private final NotaService notaService;

    public NotaController(NotaService notaService) {
        this.notaService = notaService;
    }

    @Operation(summary = "Crear nota", description = "Crea una nueva nota para el usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public ResponseEntity<NotaResponseDTO> crearNota(@Valid @RequestBody NotaRequestDTO notaRequestDTO) {
        NotaResponseDTO notaCreada = notaService.crearNota(notaRequestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(notaCreada.getId())
                .toUri();
        return ResponseEntity.created(location).body(notaCreada);
    }

    @Operation(summary = "Listar mis notas", description = "Obtiene todas las notas del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notas obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasPorUsuario() {
        return ResponseEntity.ok(notaService.obtenerNotasPorUsuario());
    }

    @Operation(summary = "Editar nota", description = "Actualiza título y/o contenido de una nota propia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nota actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada"),
            @ApiResponse(responseCode = "403", description = "No eres el propietario de esta nota"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<NotaResponseDTO> editarNota(@PathVariable Long id,
                                                      @Valid @RequestBody NotaUpdateDTO notaUpdateDTO) {
        NotaResponseDTO notaActualizada = notaService.editarNota(id, notaUpdateDTO);
        return ResponseEntity.ok(notaActualizada);
    }

    @Operation(summary = "Eliminar nota", description = "Elimina permanentemente una nota propia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Nota eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada"),
            @ApiResponse(responseCode = "403", description = "No eres el propietario de esta nota"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) {
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar notas", description = "Busca notas que contienen la keyword en título o contenido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas encontradas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<NotaResponseDTO>> buscarNotas(@RequestParam String q) {
        return ResponseEntity.ok(notaService.buscarNotas(q));
    }

    @Operation(summary = "Notas recientes", description = "Obtiene las últimas N notas ordenadas por fecha de creación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas recientes obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/recientes")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasRecientes(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(notaService.obtenerNotasRecientes(limit));
    }

    @Operation(summary = "Notas por categoría", description = "Filtra notas por categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas filtradas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(notaService.obtenerNotasPorCategoria(categoria));
    }

    @Operation(summary = "Estadísticas", description = "Obtiene estadísticas de las notas del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = EstadisticasResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasResponseDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(notaService.obtenerEstadisticas());
    }

    @Operation(summary = "Notas favoritas", description = "Obtiene las notas marcadas como favoritas en el perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas favoritas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/favoritas")
    public ResponseEntity<List<NotaResponseDTO>> obtenerNotasFavoritas() {
        return ResponseEntity.ok(notaService.obtenerNotasFavoritas());
    }
}