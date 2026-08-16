package EnergiAI.demo.service;

import EnergiAI.demo.client.DataScienceClient;
import EnergiAI.demo.client.GeminiClient;
import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.dto.PrediccionResponse;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Feature: joule-ia-ml-integration, Property 6: Aplicación de valores por defecto en Backend
 * Validates: Requirements 3.3, 5.4
 *
 * Feature: joule-ia-ml-integration, Property 8: Cálculo de costo estimado
 * Validates: Requirements 6.1
 */
class AnalisisServicePropertyTest {

    private static final List<String> VALID_TIPOS = List.of("Casa", "Oficina", "Apartamento", "Comercio");

    @Property(tries = 200)
    void defaultsAreAppliedToNullOptionalFields(
            @ForAll("requestWithNullOptionals") AnalisisRequest request) {

        AnalisisEnergeticoRepository repository = mock(AnalisisEnergeticoRepository.class);
        DataScienceClient dataScienceClient = mock(DataScienceClient.class);
        GeminiClient geminiClient = mock(GeminiClient.class);

        when(dataScienceClient.obtenerPrediccion(any()))
                .thenReturn(new PrediccionResponse("Eficiente", 0.85, List.of("Recomendacion")));
        when(repository.save(any())).thenReturn(null);

        AnalisisService service = new AnalisisService(repository, dataScienceClient, geminiClient);
        service.procesarAnalisisEnergetico(request);

        ArgumentCaptor<AnalisisRequest> captor = ArgumentCaptor.forClass(AnalisisRequest.class);
        verify(dataScienceClient).obtenerPrediccion(captor.capture());
        AnalisisRequest captured = captor.getValue();

        assert captured.getUso_horario_pico() != null && captured.getUso_horario_pico() == 0 :
                "uso_horario_pico default should be 0";
        assert captured.getAntiguedad_inmueble() != null && captured.getAntiguedad_inmueble() == 10 :
                "antiguedad_inmueble default should be 10";
        assert captured.getTiene_aire_acondicionado() != null && captured.getTiene_aire_acondicionado() == 0 :
                "tiene_aire_acondicionado default should be 0";
        assert captured.getTiene_calentador_electrico() != null && captured.getTiene_calentador_electrico() == 0 :
                "tiene_calentador_electrico default should be 0";
        assert captured.getElectrodomesticos_eficientes() != null && captured.getElectrodomesticos_eficientes() == 0 :
                "electrodomesticos_eficientes default should be 0";
    }

    @Property(tries = 200)
    void costoEstimadoEqualsConsumoTimes075(
            @ForAll("validRequest") AnalisisRequest request) {

        AnalisisEnergeticoRepository repository = mock(AnalisisEnergeticoRepository.class);
        DataScienceClient dataScienceClient = mock(DataScienceClient.class);
        GeminiClient geminiClient = mock(GeminiClient.class);

        when(dataScienceClient.obtenerPrediccion(any()))
                .thenReturn(new PrediccionResponse("Eficiente", 0.9, List.of()));
        when(repository.save(any())).thenReturn(null);

        AnalisisService service = new AnalisisService(repository, dataScienceClient, geminiClient);
        AnalisisResponse response = service.procesarAnalisisEnergetico(request);

        double expectedCosto = request.getConsumo_kwh() * 0.75;
        assert Math.abs(response.getCosto_estimado() - expectedCosto) < 0.001 :
                "Expected costo " + expectedCosto + " but got " + response.getCosto_estimado();
    }

    @Provide
    Arbitrary<AnalisisRequest> validRequest() {
        Arbitrary<Double> consumo = Arbitraries.doubles().between(50.0, 2000.0);
        Arbitrary<String> tipo = Arbitraries.of(VALID_TIPOS);
        Arbitrary<Integer> personas = Arbitraries.integers().between(1, 10);
        Arbitrary<Integer> equipos = Arbitraries.integers().between(1, 20);
        Arbitrary<Double> horas = Arbitraries.doubles().between(0.0, 24.0);
        Arbitrary<Integer> usoPico = Arbitraries.of(0, 1);
        Arbitrary<Integer> antiguedad = Arbitraries.integers().between(2, 31);
        Arbitrary<Integer> aire = Arbitraries.of(0, 1);
        Arbitrary<Integer> calentador = Arbitraries.of(0, 1);
        Arbitrary<Integer> eficientes = Arbitraries.of(0, 1);

        return Combinators.combine(consumo, tipo, personas, equipos, horas)
                .flatAs((c, t, p, e, h) ->
                        Combinators.combine(usoPico, antiguedad, aire, calentador, eficientes)
                                .as((up, ant, ai, cal, ef) ->
                                        new AnalisisRequest(c, t, p, e, h, up, ant, ai, cal, ef)));
    }

    @Provide
    Arbitrary<AnalisisRequest> requestWithNullOptionals() {
        Arbitrary<Double> consumo = Arbitraries.doubles().between(50.0, 2000.0);
        Arbitrary<String> tipo = Arbitraries.of(VALID_TIPOS);
        Arbitrary<Integer> personas = Arbitraries.integers().between(1, 10);
        Arbitrary<Integer> equipos = Arbitraries.integers().between(1, 20);
        Arbitrary<Double> horas = Arbitraries.doubles().between(0.0, 24.0);

        return Combinators.combine(consumo, tipo, personas, equipos, horas)
                .as((c, t, p, e, h) ->
                        new AnalisisRequest(c, t, p, e, h, null, null, null, null, null));
    }
}
