import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { JogoService } from '../../services/jogo.service';
import { UsuarioService } from '../../services/usuario.service';
import { PagamentoService } from '../../services/pagamento.service';
import { DespesaService } from '../../services/despesa.service';
import { TeamStateService } from '../../services/team-state.service';
import { fmt } from '../../config/constants';
import { Jogo } from '../../models/jogo.model';
import { Pagamento } from '../../models/pagamento.model';
import { Despesa } from '../../models/despesa.model';
import { Usuario } from '../../models/usuario.model';

const CIRC = 2 * Math.PI * 46;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  loading = true;

  jogos: Jogo[] = [];
  usuarios: Usuario[] = [];
  pendentes: Pagamento[] = [];
  despesas: Despesa[] = [];

  constructor(
    private jogoService: JogoService,
    private usuarioService: UsuarioService,
    private pagamentoService: PagamentoService,
    private despesaService: DespesaService,
    private teamState: TeamStateService
  ) {}

  ngOnInit(): void {
    const timeId = this.teamState.timeId!;
    forkJoin({
      jogos:    this.jogoService.findByTime(timeId).pipe(catchError(() => of<Jogo[]>([]))),
      usuarios: this.usuarioService.findByTime(timeId).pipe(catchError(() => of<Usuario[]>([]))),
      pendentes: this.pagamentoService.findPendentesByTime(timeId).pipe(catchError(() => of<Pagamento[]>([]))),
      despesas: this.despesaService.findByTime(timeId).pipe(catchError(() => of<Despesa[]>([])))
    }).subscribe(data => {
      this.jogos     = data.jogos;
      this.usuarios  = data.usuarios;
      this.pendentes = data.pendentes.map(p => ({
        ...p,
        statusPagamento: (p.statusPagamento ?? (p as any).status) as any,
        tipoPagamento:   (p.tipoPagamento   ?? (p as any).tipo)  as any
      }));
      this.despesas  = data.despesas;
      this.loading  = false;
    });
  }

  /* ── Jogos ────────────────────────────── */
  get agendados(): Jogo[] {
    return this.jogos
      .filter(j => (j.statusJogo as string)?.toUpperCase() === 'AGENDADO')
      .sort((a, b) => new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime());
  }

  get proximoJogo(): Jogo | null {
    return this.agendados[0] ?? null;
  }

  /* ── Resultados (gráfico) ─────────────── */
  get resultadoStats() {
    const finalizados = this.jogos.filter(j => (j.statusJogo as string)?.toUpperCase() === 'FINALIZADO');
    return {
      vitorias: finalizados.filter(j => j.resultado === 'VITORIA').length,
      empates:  finalizados.filter(j => j.resultado === 'EMPATE').length,
      derrotas: finalizados.filter(j => j.resultado === 'DERROTA').length,
      total:    finalizados.length,
      comResultado: finalizados.filter(j => !!j.resultado).length
    };
  }

  get donutSegments(): { color: string; label: string; count: number; dasharray: string; dashoffset: number }[] {
    const { vitorias, empates, derrotas, comResultado } = this.resultadoStats;
    if (comResultado === 0) return [];
    const GAP = 3;
    const items = [
      { count: vitorias, color: '#22c55e', label: 'Vitórias' },
      { count: empates,  color: '#f59e0b', label: 'Empates' },
      { count: derrotas, color: '#ef4444', label: 'Derrotas' }
    ];
    let cumulative = 0;
    return items.map(s => {
      const len = (s.count / comResultado) * CIRC;
      const dash = Math.max(0, len - GAP);
      const offset = CIRC * 0.25 - cumulative;
      cumulative += len;
      return { ...s, dasharray: `${dash} ${CIRC}`, dashoffset: offset };
    });
  }

  get taxaVitoria(): number {
    const { vitorias, comResultado } = this.resultadoStats;
    return comResultado > 0 ? Math.round((vitorias / comResultado) * 100) : 0;
  }

  /* ── Artilheiros ──────────────────────── */
  get artilheiros(): Usuario[] {
    return [...this.usuarios]
      .filter(u => u.ativo)
      .sort((a, b) => (b.gols ?? 0) - (a.gols ?? 0))
      .slice(0, 5);
  }

  get temDadosGols(): boolean {
    return this.usuarios.some(u => (u.gols ?? 0) > 0);
  }

  /* ── Financeiro ───────────────────────── */
  get jogadoresAtivos(): number {
    return this.usuarios.filter(u => u.ativo).length;
  }

  get totalPendenteValor(): number {
    return this.pendentes.reduce((s, p) => s + p.valor, 0);
  }

  get totalMensalidadesPendentes(): number {
    return this.pendentes
      .filter(p => p.tipoPagamento === 'MENSALIDADE')
      .reduce((s, p) => s + p.valor, 0);
  }

  get totalEventosPendentes(): number {
    return this.pendentes
      .filter(p => p.tipoPagamento === 'EVENTO')
      .reduce((s, p) => s + p.valor, 0);
  }

  get despesasMes(): number {
    const mes = new Date().toISOString().slice(0, 7);
    return this.despesas
      .filter(d => d.mesReferencia.slice(0, 7) === mes)
      .reduce((s, d) => s + d.valor, 0);
  }

  get recentesPendentes(): Pagamento[] {
    return this.pendentes.slice(0, 6);
  }

  fmt = fmt;

  formatDataHora(d: string): string {
    return new Date(d).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
  }

  formatMes(d?: string): string {
    if (!d) return '';
    const [year, month] = d.slice(0, 7).split('-');
    const meses = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    return `${meses[+month - 1]}/${year}`;
  }

  iniciais(nome: string): string {
    return nome.trim().split(/\s+/).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }
}
