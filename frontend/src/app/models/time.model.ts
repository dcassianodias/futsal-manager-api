export interface Time {
  id: string;
  nome: string;
  valorMensalidade?: number;
  ativo: boolean;
  codigo: string;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface TimeCreateRequest {
  nome: string;
  valorMensalidade?: number;
}

export interface TimeUpdateRequest {
  nome?: string;
  valorMensalidade?: number;
  ativo?: boolean;
}
