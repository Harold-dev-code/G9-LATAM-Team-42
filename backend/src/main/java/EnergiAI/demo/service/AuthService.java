package EnergiAI.demo.service;

import EnergiAI.demo.dto.AuthResponse;
import EnergiAI.demo.dto.LoginRequest;
import EnergiAI.demo.dto.RegisterRequest;
import EnergiAI.demo.model.Usuario;
import EnergiAI.demo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con ese email.");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .build();

        usuario = usuarioRepository.save(usuario);

        return new AuthResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                "Registro exitoso"
        );
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Email o contraseña incorrectos.");
        }

        return new AuthResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                "Login exitoso"
        );
    }
}
