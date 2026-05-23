export type TipoDespesa = 'ALUGUEL_QUADRA' | 'UNIFORME' | 'EVENTO' | 'OUTROS';

export interface Despesa {
  id: string;
  timeId: string;
  descricao: string;
  valor: number;
  mesReferencia: string;
  tipoDespesa: TipoDespesa;
}

export interface DespesaRequest {
  timeId: string;
  descricao: string;
  valor: number;
  mesReferencia: string;
  tipoDespesa: TipoDespesa;
}
