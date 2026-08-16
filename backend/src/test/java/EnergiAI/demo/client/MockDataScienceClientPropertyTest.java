package EnergiAI.demo.client;

import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.PrediccionResponse;
import net.jqwik.api.*;

import java.util.List;
import java.util.Set;

/**
 * Feature: joule-ia-ml-integration, Property 7: Contrato de salida del MockDataScienceClient
 * Validates: Requirements 5.1, 5.2
 */
class MockDataScienceClientPropertyTest {

    private static final Set<String> VALID_CATEGORIAS = Set.of("Eficiente", "Moderado", "Ineficiente");
    private static final List<String> VALID_TIPOS = List.of("Casa", "Oficina", "Apartamento", "Comercio");

    private final MockDataScienceClient client = new MockDataScienceClient();

    @Property(tries = 200)
    void categoriaIsOneOfThreeValidValues(@ForAll("validRequest") AnalisisRequest request) {
        PrediccionResponse response = client.obtenerPrediccion(request);
        assert VALID_CATEGORIAS.contains(response.getCategoria()) :
                "Categoria '" + response.getCategoria() + "' is not valid";
    }

    @Property(tries = 200)
    void probabilidadIsInValidRange(@ForAll("validRequest") AnalisisRequest request) {
        PrediccionResponse response = client.obtenerPrediccion(request);
        assert response.getProbabilidad() >= 0.0 && response.getProbabilidad() <= 1.0 :
                "Probabilidad " + response.getProbabilidad() + " out of range [0,1]";
    }

    @Property(tries = 200)
    void responseIsNeverNull(@ForAll("validRequest") AnalisisRequest request) {
        PrediccionResponse response = client.obtenerPrediccion(request);
        assert response != null : "Response should not be null";
        assert response.getCategoria() != null : "Categoria should not be null";
        assert response.getProbabilidad() != null : "Probabilidad should not be null";
    }

    @Property(tries = 200)
    void nullOptionalFieldsDoNotCauseErrors(@ForAll("requestWithNullOptionals") AnalisisRequest request) {
        PrediccionResponse response = client.obtenerPrediccion(request);
        assert VALID_CATEGORIAS.contains(response.getCategoria());
        assert response.getProbabilidad() >= 0.0 && response.getProbabilidad() <= 1.0;
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

        // Combine in two groups (max 8 per combine) then merge
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
