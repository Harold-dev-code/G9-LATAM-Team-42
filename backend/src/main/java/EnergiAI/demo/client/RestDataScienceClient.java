package EnergiAI.demo.client;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.PrediccionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("prod | dev")
public class RestDataScienceClient implements DataScienceClient {

    private final RestClient restClient;

    public RestDataScienceClient(
            @Value("${datascience.service.url}") String baseUrl,
            @Value("${datascience.service.connect-timeout:5000}") int connectTimeout,
            @Value("${datascience.service.read-timeout:10000}") int readTimeout) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(clientHttpRequestFactory(connectTimeout, readTimeout))
                .build();
    }

    @Override
    public PrediccionResponse obtenerPrediccion(AnalisisRequest request) {
        Map<String, Object> body = buildRequestBody(request);

        return restClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, response) -> {
                    String message = new String(response.getBody().readAllBytes());
                    throw new RuntimeException(message);
                })
                .onStatus(status -> status.is5xxServerError(), (req, response) -> {
                    throw new RuntimeException("Servicio de predicción no disponible");
                })
                .body(PrediccionResponse.class);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    private Map<String, Object> buildRequestBody(AnalisisRequest request) {
        Map<String, Object> body = new HashMap<>();

        // Required fields
        body.put("consumo_kwh", request.getConsumo_kwh());
        body.put("tipo_inmueble", request.getTipo_inmueble());
        body.put("personas_vivienda", request.getPersonas_vivienda());
        body.put("cantidad_equipos", request.getCantidad_equipos());
        body.put("horas_alto_consumo", request.getHoras_alto_consumo());

        // Optional fields with defaults for nulls
        body.put("uso_horario_pico",
                request.getUso_horario_pico() != null ? request.getUso_horario_pico() : 0);
        body.put("antiguedad_inmueble",
                request.getAntiguedad_inmueble() != null ? request.getAntiguedad_inmueble() : 10);
        body.put("tiene_aire_acondicionado",
                request.getTiene_aire_acondicionado() != null ? request.getTiene_aire_acondicionado() : 0);
        body.put("tiene_calentador_electrico",
                request.getTiene_calentador_electrico() != null ? request.getTiene_calentador_electrico() : 0);
        body.put("electrodomesticos_eficientes",
                request.getElectrodomesticos_eficientes() != null ? request.getElectrodomesticos_eficientes() : 0);

        return body;
    }
}
