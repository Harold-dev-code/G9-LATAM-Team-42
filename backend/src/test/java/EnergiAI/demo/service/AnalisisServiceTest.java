package EnergiAI.demo.service;

import EnergiAI.demo.client.DataScienceClient;
import EnergiAI.demo.dto.AnalisisRequest;
import EnergiAI.demo.dto.AnalisisResponse;
import EnergiAI.demo.dto.PrediccionResponse;
import EnergiAI.demo.exception.AnalisisDuplicadoException;
import EnergiAI.demo.model.Inmueble;
import EnergiAI.demo.model.Usuario;
import EnergiAI.demo.repository.AnalisisEnergeticoRepository;
import EnergiAI.demo.repository.InmuebleRepository;
import EnergiAI.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnalisisServiceTest {
    private final AnalisisEnergeticoRepository repository = Mockito.mock(AnalisisEnergeticoRepository.class);
    private final DataScienceClient dataScienceClient = Mockito.mock(DataScienceClient.class);
    private final EnergiAI.demo.client.GeminiClient geminiClient = Mockito.mock(EnergiAI.demo.client.GeminiClient.class);
    private final TipoDeCambioService tipoDeCambioService = Mockito.mock(TipoDeCambioService.class);
    private final InmuebleRepository inmuebleRepository = Mockito.mock(InmuebleRepository.class);
    private final UsuarioRepository usuarioRepository = Mockito.mock(UsuarioRepository.class);

    private final AnalisisService analisisService = new AnalisisService(repository, dataScienceClient, geminiClient, tipoDeCambioService);

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

        @Test
        @DisplayName("Debe rechazar dos análisis del mismo inmueble en el mismo mes y año")
        void procesar_MismoInmuebleMismoPeriodo_RetornarConflicto(){
        Usuario usuario = Usuario.builder().id(7L).email("test@example.com").build();
        Inmueble inmueble = Inmueble.builder().id(11L).usuario(usuario).nombre("Casa Principal").build();
        Mockito.when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        Mockito.when(inmuebleRepository.findByUsuarioAndNombre(7L, "Casa Principal"))
            .thenReturn(Optional.of(inmueble));
        Mockito.when(inmuebleRepository.save(inmueble)).thenReturn(inmueble);
        Mockito.when(repository.existsByInmuebleIdAndMesFacturadoAndAnioFacturado(11L, 8, 2026))
            .thenReturn(true);

        AnalisisRequest request = new AnalisisRequest(
            500.0, "Casa", "Colombia", "COP", "mensual", "Residencial",
            4, 8, 5.0, 0, 10, 0, 0, 0, 0.75);
        request.setNombre_inmueble("Casa Principal");
        request.setMes_facturado(8);
        request.setAnio_facturado(2026);

        AnalisisService service = new AnalisisService(
            repository, dataScienceClient, geminiClient, tipoDeCambioService,
            inmuebleRepository, usuarioRepository);

        assertThrows(AnalisisDuplicadoException.class,
            () -> service.procesarAnalisisEnergetico(request, 7L));
        Mockito.verifyNoInteractions(dataScienceClient);
        }

}
