export function isString(value: any): value is string {
  return value instanceof String || typeof value === 'string';
}

export function isNumber(value: any): value is number {
  return (
    value instanceof Number || (typeof value === 'number' && !isNaN(value))
  );
}

export function isBoolean(value: any): value is boolean {
  return value instanceof Boolean || typeof value === 'boolean';
}
