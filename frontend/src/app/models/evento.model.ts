export interface Evento {
  id: string;
  timeId: string;
  nome: string;
  descricao?: string;
  valorSugerido?: number;
  dataInicio: string;
  dataFim: string;
  ativo: boolean;
}

export interface EventoRequest {
  timeId: string;
  nome: string;
  descricao?: string;
  valorSugerido?: number;
  dataInicio: string;
  dataFim: string;
}
