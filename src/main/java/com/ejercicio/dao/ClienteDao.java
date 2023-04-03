package com.ejercicio.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ejercicio.entities.Cliente;


public interface ClienteDao extends JpaRepository<Cliente, Long> {

    /**Para que nos de una lista de clientes y venga ordenada por un criterio */
    @Query(value = "select c from Cliente c left join fetch c.hotel")
    public List<Cliente> findAll(Sort sort);


    /**Para recuperar un listado que podemos paginar */
    @Query(value = "select c from Cliente c left join fetch c.hotel",
    countQuery = "select count(c) from Cliente c left join c.hotel")
    public Page<Cliente> findAll(Pageable pageable);

    /**Recupera un hotel por su id con sus clientes. */
    @Query(value = "select c from Cliente c left join fetch c.hotel where c.id = :id")
    public Cliente findById(long id);
    
}
