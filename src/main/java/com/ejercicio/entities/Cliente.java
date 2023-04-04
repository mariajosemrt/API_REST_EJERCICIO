package com.ejercicio.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

    //@JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent //Para validar que no se pueda poner una fecha que no sea posterior a la de hoy. De hoy hacia atrás
    private LocalDate fechaAlta;

    @NotNull
    private String imagenCliente;

    //Relaciones
    //Un cliente puede tener varias mascotas
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "cliente")
    //@JsonIgnore
    private List<Mascota> mascotas;
    
    //Varios clientes pueden estar en un hotel
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    //@JsonManagedReference
    private Hotel hotel; 

   
}
