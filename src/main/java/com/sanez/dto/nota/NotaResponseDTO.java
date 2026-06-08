package com.sanez.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotaResponseDTO {

    private Long id;
    private String titulo;
    private String contenido;
    private Long usuarioId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String categoria;
}
