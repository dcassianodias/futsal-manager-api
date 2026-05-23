export type StatusPagamento = 'PAGO' | 'PENDENTE' | 'CANCELADO';
export type TipoPagamento = 'MENSALIDADE' | 'EVENTO';

export interface Pagamento {
  id: string;
  timeId: string;
  usuarioId: string;
  eventoId?: string;
  mesReferencia?: string;
  valor: number;
  tipoPagamento: TipoPagamento;
  statusPagamento: StatusPagamento;
}

export interface PagamentoRequest {
  timeId: string;
  usuarioId: string;
  eventoId?: string;
  mesReferencia?: string;
  valor: number;
  tipoPagamento: TipoPagamento;
}

export interface GerarMensalidadesRequest {
  timeId: string;
  mesReferencia: string;
}

export interface GerarMensalidadeResponse {
  timeId: string;
  mesReferencia: string;
  totalAtletas: number;
  totalGerados: number;
  totalJaExistentes: number;
}
