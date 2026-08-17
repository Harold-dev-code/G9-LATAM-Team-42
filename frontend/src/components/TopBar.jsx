import ThemeToggle from "./ThemeToggle";

const SECTION_LABELS = {
  inicio: "Inicio",
  analisis: "Nuevo análisis",
  historial: "Historial",
  reportes: "Reportes",
};

export default function TopBar({ section, onNavigate, theme, onToggleTheme, onToggleMobileMenu, mobileMenuOpen }) {
  return (
    <header className="topbar">
      <div className="topbar-breadcrumb">
        <button
          type="button"
          className="topbar-mobile-menu"
          onClick={onToggleMobileMenu}
          aria-expanded={mobileMenuOpen}
          aria-label="Abrir menú de navegación"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>
        <span className="topbar-eyebrow mono">JOULEAI</span>
        <span className="topbar-divider" aria-hidden="true">/</span>
        <span className="topbar-title">{SECTION_LABELS[section] || "Inicio"}</span>
      </div>

      <div className="topbar-actions">
        {section !== "analisis" && (
          <button type="button" className="btn-secondary topbar-cta" onClick={() => onNavigate("analisis")}>
            + Nuevo análisis
          </button>
        )}
        <ThemeToggle theme={theme} onToggle={onToggleTheme} compact />
      </div>
    </header>
  );
}
