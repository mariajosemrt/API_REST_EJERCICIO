package com.ejercicio.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ejercicio.entities.Cliente;
import com.ejercicio.entities.Mascota;
import com.ejercicio.model.FileUploadResponse;
import com.ejercicio.services.ClienteService;
import com.ejercicio.services.MascotaService;
import com.ejercicio.utilities.FileDownloadUtil;
import com.ejercicio.utilities.FileUploadUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor

public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private MascotaService mascotaService;

    private final FileDownloadUtil fileDownloadUtil;

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

    @PostMapping( consumes = "multipart/form-data" )
    @Transactional
    public ResponseEntity<Map<String, Object>> insert
    (@Valid @RequestPart(name = "cliente") Cliente cliente, 
    BindingResult result, @RequestPart(name = "file") MultipartFile file) throws IOException {

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String, Object>> responseEntity = null;

        /** Primero: Comprobar si hay errores en el cliente recibido */
        if(result.hasErrors()) {

            //Aquí guardamos los errores
            List<String> errorMessages = new ArrayList<>();
          
            for(ObjectError error : result.getAllErrors()) {

                errorMessages.add(error.getDefaultMessage());
            }

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
            //Return para no salir del if y no guardar. Solo devolvemos mensaje error
            
        return responseEntity;

        }
        //Si no hay errores, se persiste el cliente
        //PREVIAMENTE comprobamos si hemos recibido una imagen
        if(!file.isEmpty()) {
            String fileCode = fileUploadUtil.saveFile(file.getOriginalFilename(), file); //recibe nombre del archivo y su contenido
            //Hemos lanzado una excepcion para arriba
            cliente.setImagenCliente(fileCode + "-" + file.getOriginalFilename());

            //Devolver informacion respecto al file recibido
            FileUploadResponse fileUploadResponse = FileUploadResponse
            .builder()
            .fileName(fileCode + "-" + file.getOriginalFilename())
            .downloadURI("/clientes/downloadFile/" + fileCode + "-" + file.getOriginalFilename())
            .size(file.getSize())
            .build();

            responseAsMap.put("info de la imagen", fileUploadResponse);

            //Hay que crear el metodo que responda a la URL para recuperar la imagen del servidor
        }

        Cliente clienteDB = clienteService.save(cliente);

        //Para relacionar la mascota con el cliente y que se cree en la base de datos
        List<Mascota> mascotas = mascotaService.findAll();

        mascotas.stream().filter(m -> m.getCliente() == null)
        .forEach(m -> m.setCliente(clienteDB)); 

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

        Map<String, Object> responseAsMap = new HashMap<>();
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

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
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
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("cliente", clienteDB);
                responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);
    
            } else {
                //No se ha actualizado el cliente
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

    /** Implementa filedownload para meter las imágenes */
    @GetMapping("/downloadFile/{fileCode}") //esto es un ENDPoint
    public ResponseEntity<?> downloadFile(@PathVariable(name = "fileCode") String fileCode) {

        Resource resource = null;

        try {
            resource = fileDownloadUtil.getFileAsResource(fileCode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found ", HttpStatus.NOT_FOUND);
        }

        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType)) //MediaType de spring
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
        .body(resource);

    }

}
