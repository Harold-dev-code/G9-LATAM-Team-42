package EnergiAI.demo.service;

import EnergiAI.demo.client.DataScienceClient;
import EnergiAI.demo.client.GeminiClient;
import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.dto.PrediccionResponse;
import EnergiAI.demo.exception.AnalisisDuplicadoException;
import EnergiAI.demo.exception.UsuarioNoAutenticadoException;
import EnergiAI.demo.model.AnalisisEnergetico;
import EnergiAI.demo.model.Inmueble;
import EnergiAI.demo.model.Usuario;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import EnergiAI.demo.repository.InmuebleRepository;
import EnergiAI.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AnalisisService {

    private static final Map<String, String> MONEDAS_POR_PAIS = Map.ofEntries(
            Map.entry("Colombia", "COP"), Map.entry("México", "MXN"),
            Map.entry("Chile", "CLP"), Map.entry("Argentina", "ARS"),
            Map.entry("Perú", "PEN"), Map.entry("Brasil", "BRL"),
            Map.entry("Uruguay", "UYU"), Map.entry("Ecuador", "USD"),
            Map.entry("Guatemala", "GTQ"), Map.entry("Honduras", "HNL"),
            Map.entry("Paraguay", "PYG"), Map.entry("Bolivia", "BOB"),
            Map.entry("Costa Rica", "CRC"), Map.entry("República Dominicana", "DOP"),
            Map.entry("Venezuela", "VES"));

    private final AnalisisEnergeticoRepository repository;
    private final DataScienceClient dataScienceClient;
    private final GeminiClient geminiClient;
    private final TipoDeCambioService tipoDeCambioService;
    private final InmuebleRepository inmuebleRepository;
    private final UsuarioRepository usuarioRepository;

    public AnalisisService(AnalisisEnergeticoRepository repository,
                           DataScienceClient dataScienceClient,
                           GeminiClient geminiClient,
                           TipoDeCambioService tipoDeCambioService) {
        this(repository, dataScienceClient, geminiClient, tipoDeCambioService, null, null);
    }

    @Autowired
    public AnalisisService(AnalisisEnergeticoRepository repository,
                           DataScienceClient dataScienceClient,
                           GeminiClient geminiClient,
                           TipoDeCambioService tipoDeCambioService,
                           InmuebleRepository inmuebleRepository,
                           UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.dataScienceClient = dataScienceClient;
        this.geminiClient = geminiClient;
        this.tipoDeCambioService = tipoDeCambioService;
        this.inmuebleRepository = inmuebleRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AnalisisResponse procesarAnalisisEnergetico(AnalisisRequest request, Long usuarioId) {

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
        if (request.getPais() == null || request.getPais().isBlank()) {
            request.setPais("Colombia");
        }
        request.setMoneda(MONEDAS_POR_PAIS.getOrDefault(request.getPais(), "COP"));
        if (request.getPeriodo() == null || request.getPeriodo().isBlank()) {
            request.setPeriodo("mensual");
        }
        if (request.getEtiqueta() == null || request.getEtiqueta().isBlank()) {
            request.setEtiqueta("Residencial");
        }

        Inmueble inmueble = resolverInmueble(request, usuarioId);

        if (inmueble != null && repository.existsByInmuebleIdAndMesFacturadoAndAnioFacturado(
                inmueble.getId(), request.getMes_facturado(), request.getAnio_facturado())) {
            throw new AnalisisDuplicadoException("Ya existe un análisis para este inmueble en el mes y año seleccionados.");
        }
        if (usuarioId != null && inmueble == null && existeDuplicado(usuarioId, request)) {
            throw new IllegalArgumentException("Ya existe un análisis idéntico en tu historial para este país, moneda y período.");
        }

        // 2. Delegar el análisis de datos (Ya sea al Mock o a la API Python)
        PrediccionResponse prediccion = dataScienceClient.obtenerPrediccion(request);

        // Creamos una lista modificable con las recomendaciones iniciales
        List<String> recomendacionesFinales = prediccion.getRecomendaciones() != null
                ? new ArrayList<>(prediccion.getRecomendaciones())
                : new ArrayList<>();

        // 3. Calcular la estimación financiera usando la tarifa del usuario (default 0.75 USD/kWh)
        double tarifaKwh = request.getTarifa_kwh() != null ? request.getTarifa_kwh() : 0.75;
        double costo_estimado = request.getConsumo_kwh() * tarifaKwh;

        // 4. Integración con IA: Gemini se utiliza únicamente para recomendaciones
        try {
            String consejoIA = geminiClient.obtenerRecomendacionIA(
                    prediccion.getCategoria(),
                    request.getConsumo_kwh(),
                    request.getCantidad_equipos(),
                    costo_estimado);
            recomendacionesFinales.add(consejoIA);
        } catch (Exception e) {
            // Fallback según categoría
            if ("Eficiente".equalsIgnoreCase(prediccion.getCategoria())) {
                recomendacionesFinales.add(
                        "¡Excelente! Mantén tus buenos hábitos de consumo energético.");
            } else {
                recomendacionesFinales.add(
                        "Se sugiere revisar los hábitos de consumo energético para mejorar la eficiencia.");
            }
        }

        Map<String, Double> tasasCalculadas = Collections.emptyMap();

        // 5. Obtener la tasa del país seleccionado sin agregar conversiones LATAM al resultado
        try {
            Map<String, Double> tasas = tipoDeCambioService.obtenerTasasLatam("USD");
            tasasCalculadas = tasas;
        }catch (Exception e){
            System.out.println("No se pudieron obtener las tasas de cambio: " + e.getMessage());
        }

        // 6. Guardar en base de datos (incluye todos los nuevos campos)
        AnalisisEnergetico analisis = AnalisisEnergetico.builder()
            .inmueble(inmueble)
            .mesFacturado(request.getMes_facturado())
            .anioFacturado(request.getAnio_facturado())
                .consumoKwh(request.getConsumo_kwh())
                .usoHorarioPico(request.getUso_horario_pico())
                .cantidadEquipos(request.getCantidad_equipos())
                .tipoInmueble(request.getTipo_inmueble())
                .pais(request.getPais())
                .moneda(request.getMoneda())
                .periodo(request.getPeriodo())
                .etiqueta(request.getEtiqueta())
                .horasAltoConsumo(request.getHoras_alto_consumo())
                .personasVivienda(request.getPersonas_vivienda())
                .antiguedadInmueble(request.getAntiguedad_inmueble())
                .tieneAireAcondicionado(request.getTiene_aire_acondicionado())
                .tieneCalentadorElectrico(request.getTiene_calentador_electrico())
                .electrodomesticosEficientes(request.getElectrodomesticos_eficientes())
                .tarifaKwh(tarifaKwh)
                .usuarioId(usuarioId)
                .categoria(prediccion.getCategoria())
                .probabilidad(prediccion.getProbabilidad())
                .costoEstimadoMensual(costo_estimado)
                .recomendaciones(String.join(", ", recomendacionesFinales))
                .build();
        repository.save(analisis);

        // 7. Ensamblar la respuesta final
        double tasaLocal = tasasCalculadas.getOrDefault(request.getMoneda(), 1.0);
        AnalisisResponse response = new AnalisisResponse(
                prediccion.getCategoria(),
                prediccion.getProbabilidad(),
                recomendacionesFinales,
                costo_estimado
        );
        response.setCosto_estimado_usd(costo_estimado);
        response.setCosto_estimado_local(costo_estimado * tasaLocal);
        response.setPais(request.getPais());
        response.setMoneda(request.getMoneda());
        return response;
    }

    private Inmueble resolverInmueble(AnalisisRequest request, Long usuarioId) {
        if (usuarioId == null && inmuebleRepository != null && usuarioRepository != null) {
            throw new UsuarioNoAutenticadoException("Debes iniciar sesión para registrar un análisis.");
        }
        if (usuarioId == null || inmuebleRepository == null || usuarioRepository == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe."));
        String nombre = request.getNombre_inmueble().trim();
        Inmueble inmueble = inmuebleRepository.findByUsuarioAndNombre(usuarioId, nombre)
                .orElseGet(() -> Inmueble.builder().usuario(usuario).nombre(nombre).build());
        inmueble.setDireccion(request.getDireccion_inmueble());
        return inmuebleRepository.save(inmueble);
    }

    private boolean existeDuplicado(Long usuarioId, AnalisisRequest request) {
        List<AnalisisEnergetico> historial = repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        String tipo = request.getTipo_inmueble() == null ? "" : request.getTipo_inmueble().trim();
        String pais = request.getPais() == null ? "" : request.getPais().trim();
        String moneda = request.getMoneda() == null ? "" : request.getMoneda().trim();
        String periodo = request.getPeriodo() == null ? "" : request.getPeriodo().trim();
        String etiqueta = request.getEtiqueta() == null ? "" : request.getEtiqueta().trim();

        return historial.stream().anyMatch(analisis ->
                analisis.getConsumoKwh() != null && request.getConsumo_kwh() != null &&
                Double.compare(analisis.getConsumoKwh(), request.getConsumo_kwh()) == 0 &&
                analisis.getTipoInmueble() != null && analisis.getTipoInmueble().equalsIgnoreCase(tipo) &&
                analisis.getPais() != null && analisis.getPais().equalsIgnoreCase(pais) &&
                analisis.getMoneda() != null && analisis.getMoneda().equalsIgnoreCase(moneda) &&
                analisis.getPeriodo() != null && analisis.getPeriodo().equalsIgnoreCase(periodo) &&
                analisis.getEtiqueta() != null && analisis.getEtiqueta().equalsIgnoreCase(etiqueta) &&
                analisis.getPersonasVivienda() != null && analisis.getPersonasVivienda().equals(request.getPersonas_vivienda()) &&
                analisis.getCantidadEquipos() != null && analisis.getCantidadEquipos().equals(request.getCantidad_equipos()) &&
                analisis.getHorasAltoConsumo() != null && analisis.getHorasAltoConsumo().equals(request.getHoras_alto_consumo())
        );
    }

    public List<AnalisisEnergetico> obtenerHistorial(Long usuarioId) {
        if (usuarioId == null) {
            return Collections.emptyList();
        }
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public void eliminarAnalisis(Long id) {
        repository.deleteById(id);
    }
}
