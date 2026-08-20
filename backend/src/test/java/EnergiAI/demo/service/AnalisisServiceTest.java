package EnergiAI.demo.service;

import EnergiAI.demo.client.DataScienceClient;
import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.dto.PrediccionResponse;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalisisServiceTest {
    private final AnalisisEnergeticoRepository repository = Mockito.mock(AnalisisEnergeticoRepository.class);
    private final DataScienceClient dataScienceClient = Mockito.mock(DataScienceClient.class);
    private final EnergiAI.demo.client.GeminiClient geminiClient = Mockito.mock(EnergiAI.demo.client.GeminiClient.class);

    private final AnalisisService analisisService = new AnalisisService(repository, dataScienceClient, geminiClient);

    @Test
    @DisplayName("Debe clasificar como eficiente cuando el cliente devuelve categoria eficiente")
    void procesar_ConsumoBajo_RetornarEficiente(){
        AnalisisRequest request = new AnalisisRequest(150.0, "Casa", 3, 4, 2.0, 0, 10, 0, 0, 0);
        PrediccionResponse mockPrediccion = new PrediccionResponse("Eficiente", 0.30, List.of());
        Mockito.when(dataScienceClient.obtenerPrediccion(Mockito.any())).thenReturn(mockPrediccion);
        
        AnalisisResponse response = analisisService.procesarAnalisisEnergetico(request, null);

        assertEquals("Eficiente", response.getCategoria());
        assertEquals(112.5, response.getCosto_estimado()); //150 * 0.75
    }

    @Test
    @DisplayName("Debe clasificar como ineficiente cuando el cliente devuelve categoria ineficiente")
    void procesar_ConsumoAltoYHorarioPico_RetornarIneficiente(){
        AnalisisRequest request = new AnalisisRequest(500.0, "Oficina", 4, 15, 8.0, 1, 10, 0, 0, 0);
        PrediccionResponse mockPrediccion = new PrediccionResponse("Ineficiente", 0.789, List.of("Recomendacion"));
        Mockito.when(dataScienceClient.obtenerPrediccion(Mockito.any())).thenReturn(mockPrediccion);

        AnalisisResponse response = analisisService.procesarAnalisisEnergetico(request, null);

        assertEquals("Ineficiente", response.getCategoria());
        assertEquals(0.789, response.getProbabilidad());
    }

}
