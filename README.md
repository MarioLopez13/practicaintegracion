<<<<<<< HEAD
# Progreso 2 Integracion - Mario Lopez

## 1. Nombre del estudiante
Mario Lopez

## 2. Descripcion breve de la solucion
Esta solucion implementa una integracion minima para Salud360. La API REST recibe solicitudes de citas medicas, valida los datos y, si la cita es correcta, Apache Camel orquesta el flujo hacia RabbitMQ y hacia un archivo CSV para el sistema legado de auditoria.

## 3. Tecnologias utilizadas
- Java 21
- Spring Boot 3.3.5
- Apache Camel 4.8.1
- RabbitMQ
- Docker Compose
- Maven
- Postman o curl

## 4. Instrucciones para levantar RabbitMQ
Este proyecto usa puertos diferentes para no afectar otros proyectos Docker.

```bash
docker compose up -d
```

RabbitMQ Management:

```text
http://localhost:15673
usuario: guest
clave: guest
```

Puerto AMQP usado por la aplicacion:

```text
localhost:5673
```

## 5. Instrucciones para ejecutar la aplicacion

```bash
mvn spring-boot:run
```

La aplicacion se ejecuta en:

```text
http://localhost:8085
```

## 6. Endpoint disponible

```text
POST /api/citas
```

URL completa:

```text
http://localhost:8085/api/citas
```

## 7. Ejemplo de request valido

```json
{
  "idCita": "CITA-1001",
  "paciente": "Ana Torres",
  "correo": "ana.torres@email.com",
  "especialidad": "Cardiologia",
  "fechaCita": "2026-06-15",
  "sede": "Centro Norte",
  "valor": 45.50
}
```

## 8. Ejemplo de request invalido

```json
{
  "idCita": "CITA-1002",
  "paciente": "Luis Perez",
  "correo": "luis@email.com",
  "especialidad": "Medicina General",
  "fechaCita": "2026-06-16",
  "sede": "Centro Sur",
  "valor": 0
}
```

## 9. Explicacion de patrones aplicados

### Point-to-Point
Se aplica en el envio del comando de facturacion hacia `billing.queue`. Este mensaje debe ser procesado por un solo consumidor porque la orden de cobro no debe duplicarse.

### Publish/Subscribe
Se aplica en el exchange `appointments.events`. El mismo evento de cita confirmada se distribuye a `notifications.queue` y `analytics.queue`, porque varios sistemas necesitan reaccionar al mismo evento.

### Transferencia de archivos
Se aplica para el sistema legado de auditoria, porque este sistema no tiene API ni mensajeria. Por eso se genera el archivo `data/outbox/auditoria-citas.csv`.

### Manejo de errores
Las solicitudes invalidas no se envian a RabbitMQ ni al CSV de auditoria. El error se registra en `data/errors/citas-rechazadas.log` con fecha, id de cita y motivo.

## 10. Evidencia esperada para verificar funcionamiento
- Captura de la aplicacion ejecutandose.
- Captura de RabbitMQ levantado en Docker.
- Captura de request valido en Postman o curl.
- Captura de respuesta exitosa.
- Captura de mensaje en `billing.queue`.
- Captura de evento en `notifications.queue`.
- Captura de evento en `analytics.queue`.
- Captura del archivo `auditoria-citas.csv` generado.
- Captura del archivo `citas-rechazadas.log` con una solicitud invalida.

## Comandos curl para probar

Request valido:

```bash
curl -X POST http://localhost:8085/api/citas \
  -H "Content-Type: application/json" \
  -d '{"idCita":"CITA-1001","paciente":"Ana Torres","correo":"ana.torres@email.com","especialidad":"Cardiologia","fechaCita":"2026-06-15","sede":"Centro Norte","valor":45.50}'
```

Request invalido:

```bash
curl -X POST http://localhost:8085/api/citas \
  -H "Content-Type: application/json" \
  -d '{"idCita":"CITA-1002","paciente":"Luis Perez","correo":"luis@email.com","especialidad":"Medicina General","fechaCita":"2026-06-16","sede":"Centro Sur","valor":0}'
```
=======
# practicaintegracion
>>>>>>> f1f7abdf68120ed123de7e7ec355f7afb3d3a547
