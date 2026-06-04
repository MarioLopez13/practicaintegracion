package edu.udla.integracion.progreso2.routes;

import edu.udla.integracion.progreso2.model.CitaRequest;
import edu.udla.integracion.progreso2.service.CitaValidationService;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CitaIntegrationRoute extends RouteBuilder {
    private final CitaValidationService validationService;

    public CitaIntegrationRoute(CitaValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public void configure() {

        onException(Exception.class)
            .handled(true)
            .log("Error controlado procesando cita: ${exception.message}")
            .setHeader("motivoError", simple("${exception.message}"))
            .to("direct:cita-rechazada");

        from("direct:procesar-cita")
            .routeId("ruta-principal-cita")
            .log("Procesando cita ${body.idCita}")
            .multicast().parallelProcessing()
                .to("direct:facturacion", "direct:evento-cita", "direct:auditoria-csv")
            .end();

        from("direct:facturacion")
            .routeId("point-to-point-facturacion")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                Map<String, Object> comando = new LinkedHashMap<>();
                comando.put("idCita", cita.getIdCita());
                comando.put("paciente", cita.getPaciente());
                comando.put("especialidad", cita.getEspecialidad());
                comando.put("valor", cita.getValor());
                comando.put("tipoMensaje", "COMANDO_FACTURAR_CITA");
                exchange.getIn().setBody(comando);
            })
            .marshal().json()
            .to("spring-rabbitmq:billing.exchange?exchangeType=direct&routingKey=billing.queue&autoDeclare=false")
            .log("Comando enviado a billing.queue");

        from("direct:evento-cita")
            .routeId("publish-subscribe-evento-cita")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                Map<String, Object> evento = new LinkedHashMap<>();
                evento.put("idCita", cita.getIdCita());
                evento.put("paciente", cita.getPaciente());
                evento.put("correo", cita.getCorreo());
                evento.put("especialidad", cita.getEspecialidad());
                evento.put("fechaCita", cita.getFechaCita());
                evento.put("sede", cita.getSede());
                evento.put("tipoEvento", "CITA_CONFIRMADA");
                exchange.getIn().setBody(evento);
            })
            .marshal().json()
            .to("spring-rabbitmq:appointments.events?exchangeType=fanout&autoDeclare=false")
            .log("Evento publicado en appointments.events");

        from("direct:auditoria-csv")
            .routeId("archivo-csv-auditoria")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                String linea = String.join(",",
                    cita.getIdCita(), cita.getPaciente(), cita.getCorreo(), cita.getEspecialidad(),
                    cita.getFechaCita(), cita.getSede(), String.valueOf(cita.getValor())
                );
                exchange.getIn().setBody(linea + System.lineSeparator());
            })
            .to("file:data/outbox?fileName=auditoria-citas.csv&fileExist=Append")
            .log("Linea agregada en auditoria-citas.csv");

        from("direct:cita-rechazada")
            .routeId("registro-errores-citas")
            .process(exchange -> {
                CitaRequest cita = exchange.getIn().getBody(CitaRequest.class);
                String idCita = cita != null && cita.getIdCita() != null ? cita.getIdCita() : "SIN_ID";
                String motivo = exchange.getIn().getHeader("motivoError", String.class);
                String linea = validationService.fechaHoraActual()
                    + " | idCita=" + idCita
                    + " | motivo=" + motivo
                    + " | payload=" + exchange.getIn().getBody(String.class)
                    + System.lineSeparator();
                exchange.getIn().setBody(linea);
            })
            .to("file:data/errors?fileName=citas-rechazadas.log&fileExist=Append")
            .log("Cita rechazada registrada en citas-rechazadas.log");

        // CONSUMIDOR DEL SISTEMA DE FACTURACIÓN
        // Delay de 15 segundos para poder tomar captura cuando el mensaje llega.
        from("spring-rabbitmq:billing.exchange?queues=billing.queue&routingKey=billing.queue&autoDeclare=false")
            .routeId("consumidor-sistema-facturacion")
            .delay(15000)
            .log("Sistema de Facturacion consumio: ${body}")
            .to("file:data/processed?fileName=facturacion-procesada.log&fileExist=Append");

        // CONSUMIDOR DEL SISTEMA DE NOTIFICACIONES
        // Delay de 15 segundos para poder tomar captura cuando el evento llega.
        from("spring-rabbitmq:appointments.events?queues=notifications.queue&autoDeclare=false")
            .routeId("consumidor-sistema-notificaciones")
            .delay(15000)
            .log("Sistema de Notificaciones consumio: ${body}")
            .to("file:data/processed?fileName=notificaciones-procesadas.log&fileExist=Append");

        // CONSUMIDOR DEL SISTEMA DE ANALÍTICA
        // Delay de 15 segundos para poder tomar captura cuando el evento llega.
        from("spring-rabbitmq:appointments.events?queues=analytics.queue&autoDeclare=false")
            .routeId("consumidor-sistema-analitica")
            .delay(15000)
            .log("Sistema de Analitica consumio: ${body}")
            .to("file:data/processed?fileName=analitica-procesada.log&fileExist=Append");
    }
}