package dev.javacadabra.acceso_datos_neo4j_rest;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

@Node
public class Persona {

    @Id @GeneratedValue private Long id;
    
    private String nombre;
    private String apellido;

    public String getNombre() {
    return nombre;
    }

    public void setNombre(String nombre) {
    this.nombre = nombre;
    }

    public String getApellido() {
    return apellido;
    }

    public void setApellido(String apellido) {
    this.apellido = apellido;
    }
}


