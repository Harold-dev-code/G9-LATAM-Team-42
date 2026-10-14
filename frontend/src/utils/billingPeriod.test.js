import { describe, expect, it } from "vitest";
import { formatBillingPeriod, getBillingPeriod, sortByBillingPeriod } from "./billingPeriod";

describe("billing period helpers", () => {
  it("formats the factured month and year without using creation date", () => {
    expect(formatBillingPeriod({
      mesFacturado: 8,
      anioFacturado: 2026,
      fechaCreacion: "2026-10-01T10:30:00",
    })).toBe("Agosto 2026");
  });

  it("reports missing periods instead of falling back to creation date", () => {
    expect(getBillingPeriod({ fechaCreacion: "2026-10-01T10:30:00" })).toBeNull();
    expect(formatBillingPeriod({ fechaCreacion: "2026-10-01T10:30:00" }))
      .toBe("Período no disponible");
  });

  it("sorts by billing year and month, placing records without a period last", () => {
    const records = [
      { id: 1, mesFacturado: 1, anioFacturado: 2026 },
      { id: 2, mesFacturado: 12, anioFacturado: 2025 },
      { id: 3, fechaCreacion: "2027-01-01T00:00:00" },
    ];

    expect(sortByBillingPeriod(records).map(({ id }) => id)).toEqual([2, 1, 3]);
    expect(sortByBillingPeriod(records, true).map(({ id }) => id)).toEqual([1, 2, 3]);
  });
});