package com.ejercicio.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ejercicio.entities.Hotel;

public interface HotelDao extends JpaRepository<Hotel, Long> {
    
}
