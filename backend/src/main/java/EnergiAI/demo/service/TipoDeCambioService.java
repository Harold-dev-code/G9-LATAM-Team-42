package EnergiAI.demo.service;

import EnergiAI.demo.dto.TipoDeCambioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TipoDeCambioService {

        private static final Map<String, Double> TASAS_DESARROLLO = Map.ofEntries(
            Map.entry("USD", 1.0), Map.entry("ARS", 1200.0), Map.entry("BOB", 6.9),
            Map.entry("BRL", 5.4), Map.entry("CLP", 950.0), Map.entry("COP", 4200.0),
            Map.entry("CRC", 510.0), Map.entry("DOP", 60.0), Map.entry("GTQ", 7.8),
            Map.entry("HNL", 25.0), Map.entry("MXN", 19.0), Map.entry("NIO", 36.8),
            Map.entry("PEN", 3.8), Map.entry("PYG", 7900.0), Map.entry("UYU", 40.0),
            Map.entry("VES", 120.0));

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public TipoDeCambioService(
            @Value("${exchangerate.api.url}") String apiUrl,
            @Value("${exchangerate.api.key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public Map<String, Double> obtenerTasasLatam(String monedaBase){
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("dummy-")) {
            return TASAS_DESARROLLO;
        }
        String url = String.format("%s/%s/latest/%s", apiUrl, apiKey, monedaBase);

        TipoDeCambioResponse response = restTemplate.getForObject(url, TipoDeCambioResponse.class);

        if (response == null || !"success".equals(response.result())){
            throw new RuntimeException("Error al comunicarse con ExchangeRate API");
        }

        // Filtrar y extraer solo los países requeridos
        Map<String, Double> tasas = response.conversionRates();

        // Usamos LinkedHashMap para mantener el orden de inserción al recorrerlo luego.
        Map<String, Double> tasasLatam = new LinkedHashMap<>();
        tasasLatam.put("ARS", tasas.getOrDefault("ARS", 0.0)); // Argentina
        tasasLatam.put("BOB", tasas.getOrDefault("BOB", 0.0)); // Bolivia
        tasasLatam.put("BRL", tasas.getOrDefault("BRL", 0.0)); // Brasil
        tasasLatam.put("CLP", tasas.getOrDefault("CLP", 0.0)); // Chile
        tasasLatam.put("COP", tasas.getOrDefault("COP", 0.0)); // Colombia
        tasasLatam.put("CRC", tasas.getOrDefault("CRC", 0.0)); // Costa Rica
        tasasLatam.put("CUP", tasas.getOrDefault("CUP", 0.0)); // Cuba
        tasasLatam.put("DOP", tasas.getOrDefault("DOP", 0.0)); // República Dominicana
        tasasLatam.put("GTQ", tasas.getOrDefault("GTQ", 0.0)); // Guatemala
        tasasLatam.put("HNL", tasas.getOrDefault("HNL", 0.0)); // Honduras
        tasasLatam.put("HTG", tasas.getOrDefault("HTG", 0.0)); // Haití
        tasasLatam.put("MXN", tasas.getOrDefault("MXN", 0.0)); // México
        tasasLatam.put("NIO", tasas.getOrDefault("NIO", 0.0)); // Nicaragua
        tasasLatam.put("PEN", tasas.getOrDefault("PEN", 0.0)); // Perú
        tasasLatam.put("PYG", tasas.getOrDefault("PYG", 0.0)); // Paraguay
        tasasLatam.put("UYU", tasas.getOrDefault("UYU", 0.0)); // Uruguay
        tasasLatam.put("VES", tasas.getOrDefault("VES", 0.0)); // Venezuela

        return tasasLatam;
    }

    public double obtenerTasa(String moneda) {
        return obtenerTasasLatam("USD").getOrDefault(moneda.toUpperCase(), 1.0);
    }
}
