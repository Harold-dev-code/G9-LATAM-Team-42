const MONTHS = [
  "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
  "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
];

export function getBillingPeriod(registro) {
  const month = Number(registro?.mesFacturado);
  const year = Number(registro?.anioFacturado);

  if (!Number.isInteger(month) || month < 1 || month > 12 || !Number.isInteger(year)) {
    return null;
  }

  return { month, year };
}

export function formatBillingPeriod(registro) {
  const period = getBillingPeriod(registro);
  return period ? `${MONTHS[period.month - 1]} ${period.year}` : "Período no disponible";
}

export function sortByBillingPeriod(registros, descending = false) {
  return [...registros].sort((left, right) => {
    const leftPeriod = getBillingPeriod(left);
    const rightPeriod = getBillingPeriod(right);

    if (!leftPeriod && !rightPeriod) return 0;
    if (!leftPeriod) return 1;
    if (!rightPeriod) return -1;

    const difference = leftPeriod.year - rightPeriod.year || leftPeriod.month - rightPeriod.month;
    return descending ? -difference : difference;
  });
}