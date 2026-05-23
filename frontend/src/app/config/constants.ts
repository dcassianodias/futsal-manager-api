export const API_URL = 'http://localhost:8080';
export const TIME_ID = '17628d2e-18e0-4c01-b867-5d3843480e69';

export function fmt(value: number): string {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
