package EnergiAI.demo.service;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalisisServiceTest {
    private final AnalisisEnergeticoRepository repository = Mockito.mock(AnalisisEnergeticoRepository.class);

    private final AnalisisService analisisService = new AnalisisService(repository);

    @Test
    @DisplayName("Debe clasificar como eficiente cuando el puntaje es menor o igual a 65")
    void procesar_ConsumoBajo_RetornarEficiente(){
        AnalisisRequest request = new AnalisisRequest(150.0, false, 4, "casa", 2);
        AnalisisResponse response = analisisService.procesarAnalisisEnergetico(request);

        assertEquals("Eficiente", response.getCategoria());
        assertEquals(112.5, response.getCosto_estimado()); //150 * 0.75
    }

    @Test
    @DisplayName("Debe clasificar como ineficiente cuando el uso en horario pico eleva el score sobre 65")
    void procesar_ConsumoAltoYHorarioPico_RetornarIneficiente(){
        AnalisisRequest request = new AnalisisRequest(500.0, true, 15, "oficina", 8);
        AnalisisResponse response = analisisService.procesarAnalisisEnergetico(request);

        assertEquals("Ineficiente", response.getCategoria());
        assertEquals(78.9, response.getProbabilidad());
    }

}
