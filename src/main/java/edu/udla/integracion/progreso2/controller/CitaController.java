package edu.udla.integracion.progreso2.controller;

import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {
    private final ProducerTemplate producerTemplate;
    private final CitaValidationService validationService;

    public CitaController(ProducerTemplate producerTemplate, CitaValidationService validationService) {
        this.producerTemplate = producerTemplate;
        this.validationService = validationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrarCita(@RequestBody CitaRequest cita) {
        String error = validationService.validar(cita);
        if (error != null) {
            producerTemplate.sendBodyAndHeader("direct:cita-rechazada", cita, "motivoError", error);
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("estado", "RECHAZADA");
            respuesta.put("mensaje", error);
            respuesta.put("idCita", cita != null ? cita.getIdCita() : null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }

        producerTemplate.sendBody("direct:procesar-cita", cita);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", "Cita recibida y enviada al flujo de integracion");
        respuesta.put("idCita", cita.getIdCita());
        return ResponseEntity.ok(respuesta);
    }
}
