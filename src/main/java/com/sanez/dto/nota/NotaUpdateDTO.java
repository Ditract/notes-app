package com.sanez.dto.nota;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotaUpdateDTO {

    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String titulo;

    @Size(max = 10000, message = "El contenido no puede superar las 1400 palabras.")
    private String contenido;

    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    private String categoria;
}
