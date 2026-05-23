import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TimeService } from '../../services/time.service';
import { JogoService } from '../../services/jogo.service';
import { DespesaService } from '../../services/despesa.service';
import { EventoService } from '../../services/evento.service';
import { Time } from '../../models/time.model';
import { Jogo, StatusJogo } from '../../models/jogo.model';
import { Despesa, TipoDespesa } from '../../models/despesa.model';
import { Evento } from '../../models/evento.model';
import { fmt } from '../../config/constants';

type Tab = 'jogos' | 'despesas' | 'eventos';
type FiltroJogo = 'TODOS' | StatusJogo;

@Component({
  selector: 'app-time-publico',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './time-publico.component.html',
  styleUrl: './time-publico.component.css'
})
export class TimePublicoComponent implements OnInit {
  time: Time | null = null;
  jogos: Jogo[] = [];
  despesas: Despesa[] = [];
  eventos: Evento[] = [];
  loading = true;
  erro = '';
  tab: Tab = 'jogos';
  filtroJogo: FiltroJogo = 'TODOS';

  constructor(
    private route: ActivatedRoute,
    private timeService: TimeService,
    private jogoService: JogoService,
    private despesaService: DespesaService,
    private eventoService: EventoService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;

    this.timeService.findById(id).subscribe({
      next: time => {
        this.time = time;
        forkJoin({
          jogos:   this.jogoService.findByTime(id).pipe(catchError(() => of([]))),
          despesas: this.despesaService.findByTime(id).pipe(catchError(() => of([]))),
          eventos:  this.eventoService.findByTime(id).pipe(catchError(() => of([])))
        }).subscribe(({ jogos, despesas, eventos }) => {
          this.jogos = jogos as Jogo[];
          this.despesas = despesas as Despesa[];
          this.eventos = eventos as Evento[];
          this.loading = false;
        });
      },
      error: () => {
        this.erro = 'Time não encontrado.';
        this.loading = false;
      }
    });
  }

  /* ── COMPUTED JOGOS ─────────────────── */
  get jogosFiltrados(): Jogo[] {
    const lista = this.filtroJogo === 'TODOS'
      ? this.jogos
      : this.jogos.filter(j => j.statusJogo === this.filtroJogo);
    return lista.sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());
  }

  get statsJogos() {
    return {
      agendados:  this.jogos.filter(j => j.statusJogo === 'AGENDADO').length,
      finalizados: this.jogos.filter(j => j.statusJogo === 'FINALIZADO').length,
      cancelados:  this.jogos.filter(j => j.statusJogo === 'CANCELADO').length
    };
  }

  get proximoJogo(): Jogo | null {
    const agora = new Date();
    return this.jogos
      .filter(j => j.statusJogo === 'AGENDADO' && new Date(j.dataHora) >= agora)
      .sort((a, b) => new Date(a.dataHora).getTime() - new Date(b.dataHora).getTime())[0] ?? null;
  }

  /* ── COMPUTED DESPESAS ──────────────── */
  get totalDespesas(): number {
    return this.despesas.reduce((s, d) => s + d.valor, 0);
  }

  get despesasMes(): number {
    const mesAtual = new Date().toISOString().slice(0, 7);
    return this.despesas
      .filter(d => d.mesReferencia === mesAtual)
      .reduce((s, d) => s + d.valor, 0);
  }

  /* ── COMPUTED EVENTOS ───────────────── */
  get eventosAtivos(): Evento[] {
    return this.eventos.filter(e => e.ativo);
  }

  /* ── FORMATADORES ───────────────────── */
  formatData(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('pt-BR', {
      weekday: 'short', day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
    });
  }

  formatDataCurta(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
  }

  formatHora(dateStr: string): string {
    return new Date(dateStr).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  diasAte(dateStr: string): string {
    const diff = new Date(dateStr).getTime() - Date.now();
    const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
    if (dias <= 0) return 'hoje';
    if (dias === 1) return 'amanhã';
    return `em ${dias} dias`;
  }

  fmt(v: number): string { return fmt(v); }

  iniciais(nome: string): string {
    return nome.trim().split(/\s+/).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }

  corAvatar(nome: string): string {
    const cores = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    return cores[Math.abs(hash) % cores.length];
  }

  labelTipo(tipo: TipoDespesa): string {
    const mapa: Record<TipoDespesa, string> = {
      ALUGUEL_QUADRA: 'Quadra', UNIFORME: 'Uniforme', EVENTO: 'Evento', OUTROS: 'Outros'
    };
    return mapa[tipo];
  }

  classeTipoDespesa(tipo: TipoDespesa): string {
    const mapa: Record<TipoDespesa, string> = {
      ALUGUEL_QUADRA: 'badge-blue', UNIFORME: 'badge-purple', EVENTO: 'badge-amber', OUTROS: 'badge-gray'
    };
    return mapa[tipo];
  }
}
