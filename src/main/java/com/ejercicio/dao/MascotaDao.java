package com.ejercicio.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ejercicio.entities.Cliente;
import com.ejercicio.entities.Mascota;



public interface MascotaDao extends JpaRepository<Mascota, Long> {
    
    long deleteByCliente (Cliente cliente);
    List<Mascota> findByCliente (Cliente cliente);
}
