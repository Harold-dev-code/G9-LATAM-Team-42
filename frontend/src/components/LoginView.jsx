import { useState } from "react";

export default function LoginView({ onLogin, onSwitchToRegister }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_URL || "http://localhost:8080"}/auth/login`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
        }
      );

      if (!response.ok) {
        const body = await response.json().catch(() => null);
        throw new Error(
          body?.messages?.[0] || body?.message || "Email o contraseña incorrectos."
        );
      }

      const data = await response.json();

      if (data.userId) {
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("userName", data.nombre || "");
      }

      onLogin(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-view">
      <div className="auth-card">
        <div className="auth-header">
          <span className="brand-mark" aria-hidden="true">⚡</span>
          <span className="brand-name">JouleAI</span>
          <h1>Iniciar sesión</h1>
          <p className="text-muted">Accede a tu cuenta de JouleAI</p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="form-field">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              placeholder="tu@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-field">
            <label htmlFor="login-password">Contraseña</label>
            <input
              id="login-password"
              type="password"
              placeholder="••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && <p className="field-error">{error}</p>}

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Ingresando…" : "Ingresar"}
          </button>
        </form>

        <p className="auth-switch">
          <button type="button" className="link-btn" onClick={() => alert("Esta función estará disponible en producción.")}>
            ¿Olvidaste tu contraseña?
          </button>
        </p>

        <p className="auth-switch">
          ¿No tienes cuenta?{" "}
          <button type="button" className="link-btn" onClick={onSwitchToRegister}>
            Crear cuenta
          </button>
        </p>
      </div>
    </div>
  );
}
