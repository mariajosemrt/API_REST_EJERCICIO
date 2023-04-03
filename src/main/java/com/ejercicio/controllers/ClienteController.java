package com.ejercicio.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ejercicio.entities.Cliente;
import com.ejercicio.services.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")

public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**Método para devolver listado de clientes paginados o no y siempre ordenados
     * por un criterio, lo que implica el uso de @RequestParam */

    @GetMapping
    public ResponseEntity<List<Cliente>> findAll
    (@RequestParam(name = "page", required = false) Integer page,
    @RequestParam(name = "size", required = false) Integer size) {

        ResponseEntity<List<Cliente>> responseEntity = null;

        //Comprobamos si hemos recibido paginas o no
        List<Cliente> clientes = new ArrayList<>();

        //Criterio de ordenamiento fuera del if para que sirva tanto con paginacion
        //como sin
        Sort sortByNombre = Sort.by("nombre");

        //Vamos al if para la paginacion
        if( page != null && size != null) {

            try {
                Pageable pageable = PageRequest.of(page, size, sortByNombre);
                Page<Cliente> clientesPaginados = clienteService.findAll(pageable);
                clientes = clientesPaginados.getContent();
                responseEntity = new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK); 
               
            } catch (Exception e) {
                // En el catch solo podemos mandar informacion de la peticion
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
         } else {
            //Sin paginacion, pero con ordenamiento
            try {
                clientes = clienteService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Cliente>>(clientes, HttpStatus.OK);

            } catch (Exception e) {
                // TODO: handle exception
                responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }   
        }

        return responseEntity;
    }

    /**Recupera cliente por el id */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable(name = "id") Integer id) {

        ResponseEntity<Map<String, Object>> responseEntity = null;
        Map<String, Object> responseAsMap = new HashMap<>();

        try {
            Cliente cliente = clienteService.findById(id);

            if(cliente != null) {

            String successMessage = "Se ha encontrado el cliente con id: " + id;
            responseAsMap.put("mensaje", successMessage);
            responseAsMap.put("cliente", cliente);
              
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK); 
        
        } else {

            String errorMessage = "No se ha encontrado el cliente con id: " + id;
            responseAsMap.put("error", errorMessage);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_FOUND);
        }
           
        } catch (Exception e) {
            // TODO: handle exception
            String errorGrave = "Error grave";
            responseAsMap.put("error", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);

        }

        return responseEntity;
    }

    /**Crea/Persiste un nuevo cliente en la base de datos */
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> insert
    (@Valid @RequestBody Cliente cliente, BindingResult result) {

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        /** Primero: Comprobar si hay errores en el producto recibido */
        if(result.hasErrors()) {

            //Aquí guardamos los errores
            List<String> errorMessages = new ArrayList<>();
            //un for mejorado para recorrerlos creamos la coleccion, despues de los puntos estan
            //donde estan (result)y luego .getAllErrors, ahi ya nos pide que a la izq haya ObjectError
            for(ObjectError error : result.getAllErrors()) {

                errorMessages.add(error.getDefaultMessage());
            }

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
            //Return para no salir del if y no guardar. Solo devolvemos mensaje error
            
        return responseEntity;
        }

        Cliente clienteDB = clienteService.save(cliente);

        try {
            if(clienteDB != null) {
                String mensaje = "El cliente se ha creado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("cliente", clienteDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.CREATED);
    
            } else {
                //No se ha creado el cliente
                String mensaje = "El cliente no se ha creado";
                responseAsMap.put("mensaje", mensaje);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.NOT_ACCEPTABLE);
            }
            
        } catch (DataAccessException e) {
            String errorGrave = "Se ha producido un error grave" 
                                 + ", y la causa más probable puede ser" 
                                    + e.getMostSpecificCause();

            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap,
                                                                 HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    /**Actualiza un cliente en la base de datos */
    //Es basicamente igual que el de crear uno de arriba
    @PutMapping("/{id}")
    @Transactional //spring
    public ResponseEntity<Map<String, Object>> update(@Valid @RequestBody Cliente cliente,
                                    BindingResult result,
                                    @PathVariable(name = "id") Integer id) {

        Map<String, Object> resopnseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        /** Primero: Comprobar si hay errores en el cliente recibido */
        if(result.hasErrors()) {

            //Aquí guardamos los errores
            List<String> errorMessages = new ArrayList<>();

            //un for mejorado para recorrerlos creamos la coleccion, despues de los puntos estan
            //donde estan (result)y luego .getAllErrors, ahi ya nos pide que a la izq haya ObjectError
            for(ObjectError error : result.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }

            resopnseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(resopnseAsMap, HttpStatus.BAD_REQUEST);
            //Return para no salir del if y no guardar. Solo devolvemos mensaje error
            return responseEntity; 

        }
        //Si no hay errores, entonces se persiste el cliente (se guarda mi hermano)
        //al cliente que vas a persistir lo vinculamos con el id que se recibe con el cliente
        cliente.setId(id);
        Cliente clienteDB = clienteService.save(cliente);

        try {
            if(clienteDB != null) {
                String mensaje = "El cliente se ha actualizado correctamente";
                resopnseAsMap.put("mensaje", mensaje);
                resopnseAsMap.put("cliente", clienteDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(resopnseAsMap, HttpStatus.OK);
    
            } else {
                //No se ha actualizado el cliente
            }
            
        } catch (DataAccessException e) {
            String errorGrave = "Se ha producido un error grave" 
                                 + ", y la causa más probable puede ser" 
                                    + e.getMostSpecificCause();

            resopnseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(resopnseAsMap,
                                                                 HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

    /**Método para borrar un cliente de la base de datos*/
    @DeleteMapping("/{id}")
    @Transactional //spring
    public ResponseEntity<String> delete(@PathVariable(name = "id") Integer id) {

            ResponseEntity<String> responseEntity = null;

            //Primero lo recuperamos
            Cliente cliente = clienteService.findById(id);
            
        try {
            
            if(cliente != null) {
               clienteService.delete(cliente);
               responseEntity = new ResponseEntity<String>("Cliente borrado exitosamente", HttpStatus.OK);

            } else {

                responseEntity = new ResponseEntity<String>("No existe el cliente buscado", HttpStatus.NOT_FOUND);
            }
            
        } catch (DataAccessException e) {
            e.getMostSpecificCause();
            responseEntity = new ResponseEntity<String>("Error fatal", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }

}
