package EnergiAI.demo.controller;

import EnergiAI.demo.dto.AnalisisRequest;
import net.jqwik.api.*;

import java.util.List;
import java.util.Set;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

/**
 * Feature: joule-ia-ml-integration, Property 5: Validación Backend rechaza entradas inválidas
 * Validates: Requirements 3.1, 3.4
 */
class AnalisisControllerPropertyTest {

    private static final List<String> VALID_TIPOS = List.of("Casa", "Oficina", "Apartamento", "Comercio");

    private final Validator validator;

    AnalisisControllerPropertyTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Property(tries = 200)
    void validRequestProducesNoViolations(@ForAll("validRequest") AnalisisRequest request) {
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert violations.isEmpty() : "Expected no violations for valid request, got: " + violations;
    }

    @Property(tries = 200)
    void missingConsumoKwhProducesViolation(@ForAll("validRequest") AnalisisRequest request) {
        request.setConsumo_kwh(null);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for null consumo_kwh";
    }

    @Property(tries = 200)
    void consumoKwhBelowMinProducesViolation(@ForAll("consumoBelowMin") Double consumo) {
        AnalisisRequest request = new AnalisisRequest(consumo, "Casa", 4, 8, 5.0, 0, 10, 0, 0, 0);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for consumo_kwh=" + consumo;
    }

    @Property(tries = 200)
    void consumoKwhAboveMaxProducesViolation(@ForAll("consumoAboveMax") Double consumo) {
        AnalisisRequest request = new AnalisisRequest(consumo, "Casa", 4, 8, 5.0, 0, 10, 0, 0, 0);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for consumo_kwh=" + consumo;
    }

    @Property(tries = 200)
    void missingTipoInmuebleProducesViolation(@ForAll("validRequest") AnalisisRequest request) {
        request.setTipo_inmueble(null);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for null tipo_inmueble";
    }

    @Property(tries = 100)
    void invalidTipoInmuebleProducesViolation(@ForAll("invalidTipo") String tipo) {
        AnalisisRequest request = new AnalisisRequest(350.0, tipo, 4, 8, 5.0, 0, 10, 0, 0, 0);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for invalid tipo_inmueble: " + tipo;
    }

    @Property(tries = 200)
    void missingPersonasViviendaProducesViolation(@ForAll("validRequest") AnalisisRequest request) {
        request.setPersonas_vivienda(null);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for null personas_vivienda";
    }

    @Property(tries = 200)
    void personasViviendaOutOfRangeProducesViolation(@ForAll("outOfRangePersonas") Integer personas) {
        AnalisisRequest request = new AnalisisRequest(350.0, "Casa", personas, 8, 5.0, 0, 10, 0, 0, 0);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for personas_vivienda: " + personas;
    }

    @Property(tries = 200)
    void cantidadEquiposOutOfRangeProducesViolation(@ForAll("outOfRangeEquipos") Integer equipos) {
        AnalisisRequest request = new AnalisisRequest(350.0, "Casa", 4, equipos, 5.0, 0, 10, 0, 0, 0);
        Set<ConstraintViolation<AnalisisRequest>> violations = validator.validate(request);
        assert !violations.isEmpty() : "Expected violations for cantidad_equipos: " + equipos;
    }

    // --- Providers ---

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
    Arbitrary<Double> consumoBelowMin() {
        return Arbitraries.doubles().between(0.0, 49.9);
    }

    @Provide
    Arbitrary<Double> consumoAboveMax() {
        return Arbitraries.doubles().between(2000.1, 10000.0);
    }

    @Provide
    Arbitrary<String> invalidTipo() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                .filter(s -> !VALID_TIPOS.contains(s));
    }

    @Provide
    Arbitrary<Integer> outOfRangePersonas() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-10, 0),
                Arbitraries.integers().between(11, 100)
        );
    }

    @Provide
    Arbitrary<Integer> outOfRangeEquipos() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(-10, 0),
                Arbitraries.integers().between(21, 100)
        );
    }
}
