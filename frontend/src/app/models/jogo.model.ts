export type StatusJogo = 'AGENDADO' | 'FINALIZADO' | 'CANCELADO';
export type ResultadoJogo = 'VITORIA' | 'DERROTA' | 'EMPATE';

export interface Jogo {
  id: string;
  timeId: string;
  adversario: string;
  local: string;
  dataHora: string;
  statusJogo: StatusJogo;
  golsTime?: number;
  golsAdversario?: number;
  resultado?: ResultadoJogo;
  observacoes?: string;
}

export interface JogoRequest {
  timeId: string;
  adversario: string;
  local: string;
  dataHora: string;
  observacoes?: string;
}

export interface FinalizarJogoRequest {
  golsTime: number;
  golsAdversario: number;
  artilheiros: string[];
}
