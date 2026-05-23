import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { JogoService } from '../../services/jogo.service';
import { UsuarioService } from '../../services/usuario.service';
import { TeamStateService } from '../../services/team-state.service';
import { Jogo, JogoRequest, FinalizarJogoRequest } from '../../models/jogo.model';
import { Usuario } from '../../models/usuario.model';

@Component({
  selector: 'app-jogos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './jogos.component.html',
  styleUrl: './jogos.component.css'
})
export class JogosComponent implements OnInit {
  jogos: Jogo[] = [];
  jogadores: Usuario[] = [];
  loading = false;
  salvando = false;
  showPanel = false;
  showFinalizarPanel = false;
  jogoParaFinalizar: Jogo | null = null;
  erro = '';
  sucesso = '';

  form!: JogoRequest;
  finalizarForm = { golsTime: 0, golsAdversario: 0 };
  artilheiroCounts: Record<string, number> = {};

  constructor(
    private jogoService: JogoService,
    private usuarioService: UsuarioService,
    private teamState: TeamStateService
  ) {}

  ngOnInit(): void {
    this.form = this.emptyForm();
    this.carregar();
    this.carregarJogadores();
  }

  private emptyForm(): JogoRequest {
    return { timeId: this.teamState.timeId!, adversario: '', local: '', dataHora: '', observacoes: '' };
  }

  get agendados(): number { return this.jogos.filter(j => j.statusJogo === 'AGENDADO').length; }
  get finalizados(): number { return this.jogos.filter(j => j.statusJogo === 'FINALIZADO').length; }
  get cancelados(): number { return this.jogos.filter(j => j.statusJogo === 'CANCELADO').length; }

  statusClass(s: string): string {
    const map: Record<string, string> = { AGENDADO: 'badge badge-blue', FINALIZADO: 'badge badge-green', CANCELADO: 'badge badge-gray' };
    return map[s?.toUpperCase()] ?? 'badge badge-gray';
  }

  statusLabel(s: string): string {
    const map: Record<string, string> = { AGENDADO: 'Agendado', FINALIZADO: 'Finalizado', CANCELADO: 'Cancelado' };
    return map[s?.toUpperCase()] ?? s;
  }

  resultadoClass(r?: string): string {
    const map: Record<string, string> = { VITORIA: 'badge badge-green', DERROTA: 'badge badge-red', EMPATE: 'badge badge-amber' };
    return r ? (map[r] ?? 'badge badge-gray') : '';
  }

  resultadoLabel(r?: string): string {
    const map: Record<string, string> = { VITORIA: 'Vitória', DERROTA: 'Derrota', EMPATE: 'Empate' };
    return r ? (map[r] ?? r) : '';
  }

  formatDataHora(d: string): string {
    return new Date(d).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  carregar(): void {
    this.loading = true;
    this.erro = '';
    this.jogoService.findByTime(this.teamState.timeId!).subscribe({
      next: data => {
        this.jogos = data.map(j => ({ ...j, statusJogo: j.statusJogo?.toUpperCase() as any }));
        this.loading = false;
      },
      error: () => { this.erro = 'Não foi possível carregar os jogos. Verifique a conexão com o servidor.'; this.loading = false; }
    });
  }

  private carregarJogadores(): void {
    this.usuarioService.findByTime(this.teamState.timeId!).subscribe({
      next: data => { this.jogadores = data.filter(u => u.ativo); },
      error: () => {}
    });
  }

  abrirPanel(): void {
    this.form = this.emptyForm();
    this.showPanel = true;
    this.erro = '';
  }

  fecharPanel(): void { this.showPanel = false; }

  salvar(): void {
    if (!this.form.adversario.trim() || !this.form.local.trim() || !this.form.dataHora) {
      this.erro = 'Preencha todos os campos obrigatórios.';
      return;
    }
    this.salvando = true;
    this.jogoService.create(this.form).subscribe({
      next: () => {
        this.fecharPanel();
        this.sucesso = 'Jogo criado com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: () => { this.erro = 'Erro ao criar jogo.'; this.salvando = false; }
    });
  }

  abrirFinalizarPanel(jogo: Jogo): void {
    this.jogoParaFinalizar = jogo;
    this.finalizarForm = { golsTime: 0, golsAdversario: 0 };
    this.artilheiroCounts = {};
    this.showFinalizarPanel = true;
    this.erro = '';
  }

  fecharFinalizarPanel(): void {
    this.showFinalizarPanel = false;
    this.jogoParaFinalizar = null;
    this.artilheiroCounts = {};
  }

  addGol(userId: string): void {
    this.artilheiroCounts[userId] = (this.artilheiroCounts[userId] ?? 0) + 1;
  }

  removeGol(userId: string): void {
    if ((this.artilheiroCounts[userId] ?? 0) > 0) {
      this.artilheiroCounts[userId]--;
      if (this.artilheiroCounts[userId] === 0) delete this.artilheiroCounts[userId];
    }
  }

  golsJogador(userId: string): number {
    return this.artilheiroCounts[userId] ?? 0;
  }

  private buildArtilheiros(): string[] {
    return Object.entries(this.artilheiroCounts)
      .flatMap(([id, count]) => Array(count).fill(id));
  }

  confirmarFinalizar(): void {
    if (!this.jogoParaFinalizar) return;
    this.salvando = true;
    const req: FinalizarJogoRequest = {
      golsTime: this.finalizarForm.golsTime,
      golsAdversario: this.finalizarForm.golsAdversario,
      artilheiros: this.buildArtilheiros()
    };
    this.jogoService.finalizar(this.jogoParaFinalizar.id, req).subscribe({
      next: () => {
        this.fecharFinalizarPanel();
        this.sucesso = 'Jogo finalizado com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: (err: any) => {
        this.erro = err?.error?.message ?? 'Erro ao finalizar jogo.';
        this.salvando = false;
      }
    });
  }

  cancelar(id: string): void {
    if (!confirm('Cancelar este jogo? Esta ação não pode ser desfeita.')) return;
    this.jogoService.cancelar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao cancelar jogo.'; }
    });
  }
}
