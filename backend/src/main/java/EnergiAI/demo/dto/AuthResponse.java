package EnergiAI.demo.dto;

public class AuthResponse {

    private Long userId;
    private String email;
    private String nombre;
    private String message;

    public AuthResponse() {}

    public AuthResponse(Long userId, String email, String nombre, String message) {
        this.userId = userId;
        this.email = email;
        this.nombre = nombre;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
