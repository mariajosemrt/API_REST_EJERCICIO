package com.ejercicio.entities;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    private String nombre;
    private String raza;
    private Genero genero;

    public enum Genero {

        MUJER, HOMBRE, OTRO
    }
    
    @JsonFormat(pattern = "yyyy-MM-dd")      
    private LocalDate fechNacimiento;  

    //Relaciones
    //Un cliente puede tener varias mascotas
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Cliente cliente; 
    
}
