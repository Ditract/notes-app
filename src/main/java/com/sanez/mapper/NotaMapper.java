package com.sanez.mapper;


import com.sanez.dto.nota.NotaRequestDTO;
import com.sanez.dto.nota.NotaResponseDTO;
import com.sanez.dto.nota.NotaUpdateDTO;
import com.sanez.model.Nota;

public class NotaMapper {


    public static NotaResponseDTO toResponseDTO(Nota nota){
        if (nota == null) {
            return null;
        }
        NotaResponseDTO notaResponseDTO  = new NotaResponseDTO();
        notaResponseDTO.setId(nota.getId());
        notaResponseDTO.setTitulo(nota.getTitulo());
        notaResponseDTO.setContenido(nota.getContenido());
        notaResponseDTO.setUsuarioId(nota.getUsuario().getId());
        notaResponseDTO.setCreatedAt(nota.getCreatedAt());
        notaResponseDTO.setUpdatedAt(nota.getUpdatedAt());
        notaResponseDTO.setCategoria(nota.getCategoria());

        return notaResponseDTO;

    }

    public static Nota toEntity(NotaRequestDTO notaRequestDTO){
        if (notaRequestDTO == null) {
            return null;
        }
        Nota nota = new Nota();
        nota.setTitulo(notaRequestDTO.getTitulo());
        nota.setContenido(notaRequestDTO.getContenido());
        nota.setCategoria(notaRequestDTO.getCategoria());

        return nota;
    }

    public static void actualizarNotaDesdeDTO(Nota nota, NotaUpdateDTO notaUpdateDTO){
        if(notaUpdateDTO.getTitulo() != null){
            nota.setTitulo(notaUpdateDTO.getTitulo());
        }
        if(notaUpdateDTO.getContenido() != null){
            nota.setContenido(notaUpdateDTO.getContenido());
        }
        if(notaUpdateDTO.getCategoria() != null){
            nota.setCategoria(notaUpdateDTO.getCategoria());
        }

    }
}
