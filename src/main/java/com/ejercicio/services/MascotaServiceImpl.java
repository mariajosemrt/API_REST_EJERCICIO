package com.ejercicio.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ejercicio.dao.MascotaDao;
import com.ejercicio.entities.Cliente;
import com.ejercicio.entities.Mascota;

@Service
public class MascotaServiceImpl implements MascotaService {

    @Autowired
    private MascotaDao mascotaDao;

    @Override
    public List<Mascota> findAll() {
        return mascotaDao.findAll();
    }

    @Override
    public Mascota findById(long idMascota) {
        return mascotaDao.findById(idMascota).get();
    }

    @Override
    @Transactional
    public void deleteById(long idMascota) {
        mascotaDao.deleteById(idMascota);
    }

    @Override
    @Transactional
    public Mascota save(Mascota mascota) {
        return mascotaDao.save(mascota);
    }

    @Override
    @Transactional
    public void deleteByCliente(Cliente cliente) {
        mascotaDao.deleteByCliente(cliente);
    }

    @Override
    @Transactional
    public List<Mascota> findByCliente(Cliente cliente) {
        return mascotaDao.findByCliente(cliente);
    }
    
}
