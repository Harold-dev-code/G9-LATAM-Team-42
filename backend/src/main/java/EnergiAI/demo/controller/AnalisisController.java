package EnergiAI.demo.controller;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.exception.AnalisisDuplicadoException;
import EnergiAI.demo.exception.UsuarioNoAutenticadoException;
import EnergiAI.demo.model.AnalisisEnergetico;
import EnergiAI.demo.service.AnalisisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import EnergiAI.demo.dto.AnalisisHistorialResponse;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<AnalisisHistorialResponse>> obtenerHistorial(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId) {
        
        List<AnalisisEnergetico> analisisList = analisisService.obtenerHistorial(usuarioId);
        
        List<AnalisisHistorialResponse> responseList = analisisList.stream().map(a -> new AnalisisHistorialResponse(
                a.getId(),
                a.getMesFacturado(),
                a.getAnioFacturado(),
                a.getConsumoKwh(),
                a.getCategoria(),
                a.getProbabilidad(),
                a.getCostoEstimadoMensual(),
                a.getTarifaKwh(),
                a.getRecomendaciones(),
                a.getFechaCreacion(),
                a.getInmueble() != null ? a.getInmueble().getNombre() : null,
                a.getInmueble() != null ? a.getInmueble().getDireccion() : null
        )).toList();

        return ResponseEntity.ok(responseList);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un análisis del historial")
    @ApiResponse(responseCode = "204", description = "Análisis eliminado exitosamente")
    public ResponseEntity<Void> eliminarAnalisis(@PathVariable Long id) {
        analisisService.eliminarAnalisis(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(AnalisisDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarAnalisisDuplicado(AnalisisDuplicadoException exception) {
        return ResponseEntity.status(409).body(Map.of(
                "status", 409,
                "error", "Conflict",
                "message", exception.getMessage(),
                "messages", List.of(exception.getMessage())
        ));
    }

    @ExceptionHandler(UsuarioNoAutenticadoException.class)
    public ResponseEntity<Map<String, Object>> manejarUsuarioNoAutenticado(UsuarioNoAutenticadoException exception) {
        return ResponseEntity.status(401).body(Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", exception.getMessage(),
                "messages", List.of(exception.getMessage())
        ));
    }
}
