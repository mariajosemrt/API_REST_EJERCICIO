package com.ejercicio.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    private String nombre;
    private String apellidos;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAlta;

    //Relaciones
    //Un cliente puede tener varias mascotas
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "cliente")
    private List<Mascota> mascotas;
    
    //Varios clientes pueden estar en un hotel
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Hotel hotel; 

   
}
