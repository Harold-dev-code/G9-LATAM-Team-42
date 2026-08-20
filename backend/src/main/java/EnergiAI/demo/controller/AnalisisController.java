package EnergiAI.demo.controller;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.model.AnalisisEnergetico;
import EnergiAI.demo.service.AnalisisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analisis-energetico")
@Tag(name = "Análisis energético", description = "Endpoints para la evaluación y consulta de eficiencia energética")
public class AnalisisController {

    private final AnalisisService analisisService;

    public AnalisisController(AnalisisService analisisService) {
        this.analisisService = analisisService;
    }

    @PostMapping
    @Operation(summary = "Procesar análisis energético",
                description = "Recibe los datos de consumo del inmueble y devuelve la clasificación de eficiencia junto con recomendaciones.")
    @ApiResponse(responseCode = "200", description = "Análisis procesado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (error de validación)")
    public ResponseEntity<AnalisisResponse> analizarConsumo(
            @Valid @RequestBody AnalisisRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        AnalisisResponse response = analisisService.procesarAnalisisEnergetico(request, usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/historial")
    @Operation(summary = "Obtener historial de análisis",
                description = "Obtener los análisis del usuario autenticado. Si no se envía X-User-Id, retorna todos.")
    @ApiResponse(responseCode = "200", description = "Historial recuperado exitosamente")
    public ResponseEntity<List<AnalisisEnergetico>> obtenerHistorial(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        return ResponseEntity.ok(analisisService.obtenerHistorial(usuarioId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un análisis del historial")
    @ApiResponse(responseCode = "204", description = "Análisis eliminado exitosamente")
    public ResponseEntity<Void> eliminarAnalisis(@PathVariable Long id) {
        analisisService.eliminarAnalisis(id);
        return ResponseEntity.noContent().build();
    }
}
