package EnergiAI.demo.client;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.PrediccionResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementación de desarrollo del DataScienceClient que simula predicciones
 * sin conexión al Servicio Flask. Usa una heurística basada en puntaje (0-100)
 * que consume los 10 campos del AnalisisRequest expandido.
 *
 * Umbrales de categorización:
 *   score < 33  → "Eficiente"
 *   33 ≤ score ≤ 66 → "Moderado"
 *   score > 66  → "Ineficiente"
 *
 * La probabilidad se calcula en el rango [0.5, 1.0] indicando la confianza
 * en la categoría asignada.
 */
@Component
@Profile("mock")
public class MockDataScienceClient implements DataScienceClient {

    // Defaults idénticos a los del Servicio Flask
    private static final int DEFAULT_USO_HORARIO_PICO = 0;
    private static final int DEFAULT_ANTIGUEDAD_INMUEBLE = 10;
    private static final int DEFAULT_TIENE_AIRE_ACONDICIONADO = 0;
    private static final int DEFAULT_TIENE_CALENTADOR_ELECTRICO = 0;
    private static final int DEFAULT_ELECTRODOMESTICOS_EFICIENTES = 0;

    @Override
    public PrediccionResponse obtenerPrediccion(AnalisisRequest request) {
        // 1. Extraer campos obligatorios
        double consumoKwh = request.getConsumo_kwh();
        String tipoInmueble = request.getTipo_inmueble();
        int personasVivienda = request.getPersonas_vivienda();
        int cantidadEquipos = request.getCantidad_equipos();
        double horasAltoConsumo = request.getHoras_alto_consumo();

        // 2. Extraer campos opcionales con defaults para nulos
        int usoHorarioPico = request.getUso_horario_pico() != null
                ? request.getUso_horario_pico() : DEFAULT_USO_HORARIO_PICO;
        int antiguedadInmueble = request.getAntiguedad_inmueble() != null
                ? request.getAntiguedad_inmueble() : DEFAULT_ANTIGUEDAD_INMUEBLE;
        int tieneAireAcondicionado = request.getTiene_aire_acondicionado() != null
                ? request.getTiene_aire_acondicionado() : DEFAULT_TIENE_AIRE_ACONDICIONADO;
        int tieneCalentadorElectrico = request.getTiene_calentador_electrico() != null
                ? request.getTiene_calentador_electrico() : DEFAULT_TIENE_CALENTADOR_ELECTRICO;
        int electrodomesticosEficientes = request.getElectrodomesticos_eficientes() != null
                ? request.getElectrodomesticos_eficientes() : DEFAULT_ELECTRODOMESTICOS_EFICIENTES;

        // 3. Calcular score heurístico (0-100) usando todos los campos
        double score = calcularScore(consumoKwh, tipoInmueble, personasVivienda,
                cantidadEquipos, horasAltoConsumo, usoHorarioPico,
                antiguedadInmueble, tieneAireAcondicionado,
                tieneCalentadorElectrico, electrodomesticosEficientes);

        // 4. Determinar categoría según umbrales del score
        String categoria = determinarCategoria(score);

        // 5. Calcular probabilidad [0.5, 1.0] basada en la confianza en la categoría
        double probabilidad = calcularProbabilidad(score, categoria);

        // 6. Generar recomendaciones estáticas según categoría
        List<String> recomendaciones = generarRecomendaciones(categoria);

        return new PrediccionResponse(categoria, probabilidad, recomendaciones);
    }

    /**
     * Calcula un puntaje heurístico (0-100) basado en los 10 campos del request.
     * Un puntaje más alto indica mayor ineficiencia energética.
     */
    private double calcularScore(double consumoKwh, String tipoInmueble,
                                  int personasVivienda, int cantidadEquipos,
                                  double horasAltoConsumo, int usoHorarioPico,
                                  int antiguedadInmueble, int tieneAireAcondicionado,
                                  int tieneCalentadorElectrico, int electrodomesticosEficientes) {

        double score = 0.0;

        // Consumo per cápita (normalizado sobre rango 50-2000 kWh y 1-10 personas)
        double consumoPerCapita = consumoKwh / personasVivienda;
        score += (consumoPerCapita / 200.0) * 25.0; // Aporta hasta ~25 puntos

        // Horas de alto consumo (0-24h, aporta hasta ~20 puntos)
        score += (horasAltoConsumo / 24.0) * 20.0;

        // Cantidad de equipos (1-20, aporta hasta ~15 puntos)
        score += (cantidadEquipos / 20.0) * 15.0;

        // Tipo de inmueble (diferentes perfiles de consumo)
        switch (tipoInmueble) {
            case "Comercio": score += 12.0; break;
            case "Oficina": score += 9.0; break;
            case "Casa": score += 6.0; break;
            case "Apartamento": score += 3.0; break;
            default: score += 6.0; break;
        }

        // Uso en horario pico (penalización)
        if (usoHorarioPico == 1) {
            score += 8.0;
        }

        // Antigüedad del inmueble (mayor antigüedad = menos eficiencia)
        score += (antiguedadInmueble / 31.0) * 7.0;

        // Aire acondicionado (alto consumo)
        if (tieneAireAcondicionado == 1) {
            score += 6.0;
        }

        // Calentador eléctrico (alto consumo)
        if (tieneCalentadorElectrico == 1) {
            score += 5.0;
        }

        // Electrodomésticos eficientes (reduce el score)
        if (electrodomesticosEficientes == 1) {
            score -= 8.0;
        }

        // Clamp score al rango [0, 100]
        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Determina la categoría de eficiencia según el score (0-100).
     *   score < 33  → "Eficiente"
     *   33 ≤ score ≤ 66 → "Moderado"
     *   score > 66  → "Ineficiente"
     */
    private String determinarCategoria(double score) {
        if (score < 33.0) {
            return "Eficiente";
        } else if (score <= 66.0) {
            return "Moderado";
        } else {
            return "Ineficiente";
        }
    }

    /**
     * Calcula la probabilidad en [0.5, 1.0] indicando la confianza en la categoría.
     * Cuanto más alejado del borde del umbral, mayor la confianza.
     */
    private double calcularProbabilidad(double score, String categoria) {
        double distanciaAlCentro;
        switch (categoria) {
            case "Eficiente":
                // Centro de la categoría es 16.5 (rango 0-33)
                distanciaAlCentro = 1.0 - (Math.abs(score - 16.5) / 16.5);
                break;
            case "Moderado":
                // Centro de la categoría es 49.5 (rango 33-66)
                distanciaAlCentro = 1.0 - (Math.abs(score - 49.5) / 16.5);
                break;
            case "Ineficiente":
                // Centro de la categoría es 83 (rango 66-100)
                distanciaAlCentro = 1.0 - (Math.abs(score - 83.0) / 17.0);
                break;
            default:
                distanciaAlCentro = 0.5;
        }
        // Mapear distanciaAlCentro [0, 1] al rango de probabilidad [0.5, 1.0]
        double probabilidad = 0.5 + (Math.max(0.0, Math.min(1.0, distanciaAlCentro)) * 0.5);
        return Math.max(0.5, Math.min(1.0, probabilidad));
    }

    /**
     * Genera recomendaciones estáticas apropiadas según la categoría resultante.
     */
    private List<String> generarRecomendaciones(String categoria) {
        switch (categoria) {
            case "Ineficiente":
                return List.of(
                        "Reducir el uso de equipos durante los horarios pico",
                        "Evaluar equipos con alto consumo energético y considerar reemplazos eficientes",
                        "Distribuir las actividades de mayor consumo a lo largo del día",
                        "Considerar la instalación de paneles solares o energía renovable"
                );
            case "Moderado":
                return List.of(
                        "Apagar las pantallas y equipos cuando no estén en uso",
                        "Evaluar el uso de electrodomésticos con certificación de eficiencia energética"
                );
            case "Eficiente":
                return List.of(
                        "Mantener las buenas prácticas de consumo energético"
                );
            default:
                return List.of();
        }
    }
}
