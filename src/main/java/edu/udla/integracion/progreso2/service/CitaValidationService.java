package edu.udla.integracion.progreso2.service;

import edu.udla.integracion.progreso2.model.CitaRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CitaValidationService {
    public String validar(CitaRequest cita) {
        if (cita == null) return "El payload no puede estar vacio";
        if (isBlank(cita.getIdCita())) return "idCita es obligatorio";
        if (isBlank(cita.getPaciente())) return "paciente es obligatorio";
        if (isBlank(cita.getCorreo())) return "correo es obligatorio";
        if (isBlank(cita.getEspecialidad())) return "especialidad es obligatoria";
        if (isBlank(cita.getFechaCita())) return "fechaCita es obligatoria";
        if (isBlank(cita.getSede())) return "sede es obligatoria";
        if (cita.getValor() == null || cita.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            return "valor debe ser mayor a 0";
        }
        return null;
    }

    public String fechaHoraActual() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
