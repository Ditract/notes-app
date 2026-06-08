package com.sanez.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasResponseDTO {
    private long totalNotas;
    private Map<String, Long> notasPorCategoria;
    private long totalFavoritas;
    private LocalDateTime notaMasReciente;
    private LocalDateTime notaMasAntigua;
}
