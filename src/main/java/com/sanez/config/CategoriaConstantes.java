package com.sanez.config;

import java.util.List;

public final class CategoriaConstantes {
    public static final String TRABAJO = "TRABAJO";
    public static final String PERSONAL = "PERSONAL";
    public static final String IDEAS = "IDEAS";
    public static final String REUNIONES = "REUNIONES";
    public static final String TAREAS = "TAREAS";

    public static final List<String> TODAS = List.of(
        TRABAJO, PERSONAL, IDEAS, REUNIONES, TAREAS
    );
}
