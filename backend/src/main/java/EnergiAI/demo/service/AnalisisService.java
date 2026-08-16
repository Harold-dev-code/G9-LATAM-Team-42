package EnergiAI.demo.service;

import EnergiAI.demo.client.DataScienceClient;
import EnergiAI.demo.client.GeminiClient;
import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.dto.PrediccionResponse;
import EnergiAI.demo.model.AnalisisEnergetico;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalisisService {

    private final AnalisisEnergeticoRepository repository;
    private final DataScienceClient dataScienceClient;
    private final GeminiClient geminiClient;

    public AnalisisService(AnalisisEnergeticoRepository repository,
                           DataScienceClient dataScienceClient,
                           GeminiClient geminiClient) {
        this.repository = repository;
        this.dataScienceClient = dataScienceClient;
        this.geminiClient = geminiClient;
    }

    public AnalisisResponse procesarAnalisisEnergetico(AnalisisRequest request) {

        // 1. Asignar defaults a campos opcionales nulos antes de delegar
        if (request.getUso_horario_pico() == null) {
            request.setUso_horario_pico(0);
        }
        if (request.getAntiguedad_inmueble() == null) {
            request.setAntiguedad_inmueble(10);
        }
        if (request.getTiene_aire_acondicionado() == null) {
            request.setTiene_aire_acondicionado(0);
        }
        if (request.getTiene_calentador_electrico() == null) {
            request.setTiene_calentador_electrico(0);
        }
        if (request.getElectrodomesticos_eficientes() == null) {
            request.setElectrodomesticos_eficientes(0);
        }

        // 2. Delegar el análisis de datos (Ya sea al Mock o a la API Python)
        PrediccionResponse prediccion = dataScienceClient.obtenerPrediccion(request);

        // Creamos una lista modificable con las recomendaciones iniciales
        List<String> recomendacionesFinales = new ArrayList<>(prediccion.getRecomendaciones());

        // 3. Integración con IA: Si no es eficiente, le pedimos un consejo a Gemini
        if (!"Eficiente".equalsIgnoreCase(prediccion.getCategoria())) {
            try {
                String consejoIA = geminiClient.obtenerRecomendacionIA(
                        prediccion.getCategoria(),
                        request.getConsumo_kwh(),
                        request.getCantidad_equipos()
                );
                recomendacionesFinales.add(consejoIA);
            } catch (Exception e) {
                recomendacionesFinales.add(
                        "Se sugiere revisar los hábitos de consumo energético para mejorar la eficiencia.");
            }
        }

        // 4. Calcular la estimación financiera (Lógica de negocio propia del backend)
        double costo_estimado = request.getConsumo_kwh() * 0.75;

        // 5. Guardar en base de datos (incluye todos los nuevos campos)
        AnalisisEnergetico analisis = AnalisisEnergetico.builder()
                .consumoKwh(request.getConsumo_kwh())
                .usoHorarioPico(request.getUso_horario_pico())
                .cantidadEquipos(request.getCantidad_equipos())
                .tipoInmueble(request.getTipo_inmueble())
                .horasAltoConsumo(request.getHoras_alto_consumo())
                .personasVivienda(request.getPersonas_vivienda())
                .antiguedadInmueble(request.getAntiguedad_inmueble())
                .tieneAireAcondicionado(request.getTiene_aire_acondicionado())
                .tieneCalentadorElectrico(request.getTiene_calentador_electrico())
                .electrodomesticosEficientes(request.getElectrodomesticos_eficientes())
                .categoria(prediccion.getCategoria())
                .probabilidad(prediccion.getProbabilidad())
                .costoEstimadoMensual(costo_estimado)
                .recomendaciones(String.join(", ", recomendacionesFinales))
                .build();
        repository.save(analisis);

        // 6. Ensamblar la respuesta final
        return new AnalisisResponse(
                prediccion.getCategoria(),
                prediccion.getProbabilidad(),
                recomendacionesFinales,
                costo_estimado
        );
    }

    public List<AnalisisEnergetico> obtenerHistorial() {
        return repository.findAll();
    }
}
