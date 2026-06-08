package com.sanez.dto.nota;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotaRequestDTO {

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 10000, message = "El contenido no puede superar las 1400 palabras.")
    private String contenido;

    @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
    private String categoria;
}
