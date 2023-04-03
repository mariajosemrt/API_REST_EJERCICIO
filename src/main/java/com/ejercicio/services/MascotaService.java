package com.ejercicio.services;

import java.util.List;

import com.ejercicio.entities.Cliente;
import com.ejercicio.entities.Mascota;

public interface MascotaService {
    
    public List<Mascota> findAll();
    public Mascota findById(long idMascota);
    public void deleteById(long idMascota); 
    public Mascota save (Mascota mascota); 

    public void deleteByCliente (Cliente cliente);
    public List<Mascota> findByCliente (Cliente cliente);
    
}
