import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";
import { createElement } from "react";
import { describe, expect, it } from "vitest";
import CarbonFootprintIndicator from "./CarbonFootprintIndicator";

describe("CarbonFootprintIndicator", () => {
  const result = {
    pais: "Colombia",
    huella_carbono_kg: 149,
    factor_emision_kg_co2_kwh: 0.298,
    anio_factor_emision: 2024,
    fuente_factor_emision: "Ember Yearly Electricity Data",
  };

  it("shows the estimate, level, scale, and factor source", () => {
    render(createElement(CarbonFootprintIndicator, { result }));

    expect(screen.getByText("kg CO₂/mes").parentElement).toHaveTextContent("149 kg CO₂/mes");
    expect(screen.getByText("Moderado")).toBeInTheDocument();
    expect(screen.getByText(/Ember Yearly Electricity Data, 2024/)).toBeInTheDocument();
    expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "149");
    expect(screen.getByRole("progressbar").firstChild).toHaveStyle({ width: "29.8%" });
  });

  it("explains when the country has no supported emission factor", () => {
    render(createElement(CarbonFootprintIndicator, {
      result: { ...result, huella_carbono_kg: null },
    }));

    expect(screen.getByText("No disponible")).toBeInTheDocument();
    expect(screen.getByText(/Colombia/)).toBeInTheDocument();
    expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
  });

  it.each([
    [100, "Bajo"],
    [100.01, "Moderado"],
    [250, "Moderado"],
    [250.01, "Alto"],
  ])("assigns the %s kg visual threshold to level %s", (emissions, expectedLevel) => {
    render(createElement(CarbonFootprintIndicator, {
      result: { ...result, huella_carbono_kg: emissions },
    }));

    expect(screen.getByText(expectedLevel)).toBeInTheDocument();
  });

  it("caps the visual bar at 500 kg while retaining the actual estimate", () => {
    render(createElement(CarbonFootprintIndicator, {
      result: { ...result, huella_carbono_kg: 600 },
    }));

    expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "500");
    expect(screen.getByRole("progressbar")).toHaveAttribute(
      "aria-valuetext",
      "600 kg CO₂ al mes, nivel alto",
    );
    expect(screen.getByRole("progressbar").firstChild).toHaveStyle({ width: "100%" });
  });

  it("does not show the feature for older API responses without the field", () => {
    const { container } = render(createElement(CarbonFootprintIndicator, {
      result: { pais: "Colombia" },
    }));

    expect(container).toBeEmptyDOMElement();
  });
});
