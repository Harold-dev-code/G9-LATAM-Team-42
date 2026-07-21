package EnergiAI.demo.controller;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/analisis")
public class AnalisisController {

    @PostMapping("/evaluar")
    public ResponseEntity<AnalisisResponse> evaluarEficiencia(@Valid @RequestBody AnalisisRequest request) {

        // Respuesta simulada (Mock) con la nueva estructura completa:
        AnalisisResponse respuestaSimulada = new AnalisisResponse(
                "Moderado",
                0.85,
                List.of("Desconectar aparatos en standby", "Aprovechar luz natural en horas pico"),
                150000.0
        );

        return ResponseEntity.ok(respuestaSimulada);
    }
}