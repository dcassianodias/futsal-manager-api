import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PagamentoService } from '../../services/pagamento.service';
import { UsuarioService } from '../../services/usuario.service';
import { TeamStateService } from '../../services/team-state.service';
import { Pagamento, PagamentoRequest, GerarMensalidadesRequest, StatusPagamento, GerarMensalidadeResponse } from '../../models/pagamento.model';
import { Usuario } from '../../models/usuario.model';
import { fmt } from '../../config/constants';

@Component({
  selector: 'app-pagamentos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './pagamentos.component.html',
  styleUrl: './pagamentos.component.css'
})
export class PagamentosComponent implements OnInit {
  todosPagamentos: Pagamento[] = [];
  usuarios: Usuario[] = [];
  usuariosMap = new Map<string, string>();
  loading = false;
  salvando = false;
  showPanel = false;
  showGerarPanel = false;
  filtro: string = 'TODOS';
  erro = '';
  sucesso = '';

  form!: PagamentoRequest;
  gerarForm!: GerarMensalidadesRequest;

  constructor(
    private pagamentoService: PagamentoService,
    private usuarioService: UsuarioService,
    private teamState: TeamStateService
  ) {}

  ngOnInit(): void {
    this.form = this.emptyForm();
    this.gerarForm = { timeId: this.teamState.timeId!, mesReferencia: '' };
    this.carregar();
  }

  private emptyForm(): PagamentoRequest {
    return { timeId: this.teamState.timeId!, usuarioId: '', valor: 0, tipoPagamento: 'MENSALIDADE' };
  }

  get filtrados(): Pagamento[] {
    if (this.filtro === 'TODOS') return this.todosPagamentos;
    return this.todosPagamentos.filter(p => p.statusPagamento === this.filtro);
  }

  get totalPendente(): number {
    return this.todosPagamentos
      .filter(p => p.statusPagamento?.toUpperCase() === 'PENDENTE')
      .reduce((s, p) => s + p.valor, 0);
  }

  countByStatus(s: StatusPagamento): number {
    return this.todosPagamentos
      .filter(p => p.statusPagamento?.toUpperCase() === s.toUpperCase())
      .length;
  }

  statusClass(s: string): string {
    const map: Record<string, string> = {
      PAGO: 'badge badge-green',
      PENDENTE: 'badge badge-amber',
      CANCELADO: 'badge badge-gray'
    };
    return map[s?.toUpperCase()] ?? 'badge badge-gray';
  }

  statusLabel(s: string): string {
    const map: Record<string, string> = { PAGO: 'Pago', PENDENTE: 'Pendente', CANCELADO: 'Cancelado' };
    return map[s?.toUpperCase()] ?? s;
  }

  tipoLabel(t: string): string {
    const map: Record<string, string> = { MENSALIDADE: 'Mensalidade', EVENTO: 'Evento' };
    return map[t?.toUpperCase()] ?? t ?? '—';
  }

  getNome(id: string): string {
    return this.usuariosMap.get(id) ?? '—';
  }

  fmt = fmt;

  formatMes(d?: string): string {
    if (!d) return '—';
    const [year, month] = d.slice(0, 7).split('-');
    const meses = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    return `${meses[+month - 1]}/${year}`;
  }

  carregar(): void {
    this.loading = true;
    this.erro = '';
    const timeId = this.teamState.timeId!;
    forkJoin({
      pagamentos: this.pagamentoService.findByTime(timeId).pipe(catchError(() => of<Pagamento[]>([]))),
      usuarios: this.usuarioService.findByTime(timeId).pipe(catchError(() => of<Usuario[]>([])))
    }).subscribe({
      next: ({ pagamentos, usuarios }) => {
        this.todosPagamentos = pagamentos.map(p => ({
          ...p,
          statusPagamento: (p.statusPagamento ?? (p as any).status) as any,
          tipoPagamento:   (p.tipoPagamento   ?? (p as any).tipo)  as any
        }));
        this.usuarios = usuarios;
        this.usuariosMap = new Map(usuarios.map(u => [u.id, u.nome]));
        this.loading = false;
      },
      error: () => { this.erro = 'Não foi possível carregar os pagamentos.'; this.loading = false; }
    });
  }

  abrirPanel(): void {
    this.form = this.emptyForm();
    this.showPanel = true;
    this.erro = '';
  }

  fecharPanel(): void { this.showPanel = false; }

  abrirGerarPanel(): void {
    const hoje = new Date();
    this.gerarForm = {
      timeId: this.teamState.timeId!,
      mesReferencia: `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-01`
    };
    this.showGerarPanel = true;
    this.erro = '';
  }

  fecharGerarPanel(): void { this.showGerarPanel = false; }

  get mesInput(): string {
    return this.gerarForm.mesReferencia.slice(0, 7);
  }
  set mesInput(val: string) {
    this.gerarForm.mesReferencia = val ? `${val}-01` : '';
  }

  get formMesInput(): string {
    return (this.form.mesReferencia ?? '').slice(0, 7);
  }
  set formMesInput(val: string) {
    this.form.mesReferencia = val ? `${val}-01` : undefined;
  }

  salvar(): void {
    if (!this.form.usuarioId || !this.form.valor || this.form.valor <= 0) {
      this.erro = 'Selecione o jogador e informe o valor.';
      return;
    }
    this.salvando = true;
    this.pagamentoService.create(this.form).subscribe({
      next: () => {
        this.fecharPanel();
        this.sucesso = 'Pagamento criado com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: () => { this.erro = 'Erro ao criar pagamento.'; this.salvando = false; }
    });
  }

  gerarMensalidades(): void {
    if (!this.gerarForm.mesReferencia) {
      this.erro = 'Selecione o mês de referência.';
      return;
    }
    this.salvando = true;
    this.pagamentoService.gerarMensalidades(this.gerarForm).subscribe({
      next: (res: GerarMensalidadeResponse) => {
        this.fecharGerarPanel();
        this.sucesso = `${res.totalGerados} mensalidade(s) gerada(s). ${res.totalJaExistentes} já existia(m).`;
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 4000);
      },
      error: (err) => {
        this.erro = err?.error?.message ?? 'Erro ao gerar mensalidades.';
        this.salvando = false;
      }
    });
  }

  pagar(id: string): void {
    this.pagamentoService.pagar(id).subscribe({
      next: () => { this.sucesso = 'Pagamento registrado!'; this.carregar(); setTimeout(() => this.sucesso = '', 3000); },
      error: () => { this.erro = 'Erro ao registrar pagamento.'; }
    });
  }

  cancelarPagamento(id: string): void {
    if (!confirm('Cancelar este pagamento?')) return;
    this.pagamentoService.cancelar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao cancelar pagamento.'; }
    });
  }
}
