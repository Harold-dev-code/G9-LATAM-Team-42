package EnergiAI.demo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient;

    public GeminiClient(){
        this.restClient = RestClient.create();
    }

    public String obtenerRecomendacionIA(String categoria, double consumoKwh, int cantidadDeEquipos, double costoEstimadoUSD){
        String prompt = "Actúa como un experto en eficiencia energética. " +
                "El análisis energético dio la categoría: " + categoria +
                ". Tiene un consumo de " + consumoKwh + " kWh con " +
                cantidadDeEquipos + " equipos. El costo estimado mensual es USD " +
                String.format("%.2f", costoEstimadoUSD) + ". " +
                "Responde EXACTAMENTE respetando estas dos secciones separadas por saltos de línea, sin alterar los nombres de las etiquetas:\n\n" +
                "RECOMENDACION: ¡Hola! [Escribe aquí tu recomendación corta y amigable, por ejemplo comentando sobre los equipos y formas de ahorro]\n\n" +
                "CONVERSION:\n" +
                "🇦🇷 Argentina (ARS): [valor aproximado]\n" +
                "🇧🇴 Bolivia (BOB): [valor aproximado]\n" +
                "🇧🇷 Brasil (BRL): [valor aproximado]\n" +
                "🇨🇱 Chile (CLP): [valor aproximado]\n" +
                "🇨🇴 Colombia (COP): [valor aproximado]\n" +
                "🇨🇷 Costa Rica (CRC): [valor aproximado]\n" +
                "🇨🇺 Cuba (CUP): [valor aproximado]\n" +
                "🇬🇹 Guatemala (GTQ): [valor aproximado]\n" +
                "🇭🇹 Haití (HTG): [valor aproximado]\n" +
                "🇭🇳 Honduras (HNL): [valor aproximado]\n" +
                "🇲🇽 México (MXN): [valor aproximado]\n" +
                "🇳🇮 Nicaragua (NIO): [valor aproximado]\n" +
                "🇵🇾 Paraguay (PYG): [valor aproximado]\n" +
                "🇵🇪 Perú (PEN): [valor aproximado]\n" +
                "🇩🇴 República Dominicana (DOP): [valor aproximado]\n" +
                "🇺🇾 Uruguay (UYU): [valor aproximado]\n" +
                "🇻🇪 Venezuela (VES): [valor aproximado]\n\n" +
                "Asegúrate de conservar la etiqueta RECOMENDACION: al inicio y el bloque CONVERSION: tal cual se indicó.";

        String requestBody = """
                {
                    "contents": [{
                        "parts": [{"text": "%s"}]
                    }]
                }
                """.formatted(prompt);

        try {
            String responseString = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseString != null){
                ObjectMapper mapper = new ObjectMapper();
                JsonNode responseNode = mapper.readTree(responseString);

                if (responseNode.has("candidates")){
                    return responseNode.at("/candidates/0/content/parts/0/text").asText().trim();
                }
            }
        } catch (Exception e){
            System.err.println("Error al conectar con Gemini: " + e.getMessage());
            throw new RuntimeException("Gemini no disponible", e);
        }

        return "Te recomendamos revisar tus equipos de mayor consumo"; // Fallback si no hay candidates
    }
}
