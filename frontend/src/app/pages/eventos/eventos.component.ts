import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EventoService } from '../../services/evento.service';
import { TeamStateService } from '../../services/team-state.service';
import { Evento, EventoRequest } from '../../models/evento.model';
import { fmt } from '../../config/constants';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './eventos.component.html',
  styleUrl: './eventos.component.css'
})
export class EventosComponent implements OnInit {
  eventos: Evento[] = [];
  loading = false;
  salvando = false;
  showPanel = false;
  filtroAtivos = true;
  erro = '';
  sucesso = '';

  form!: EventoRequest;
  fmt = fmt;

  constructor(private eventoService: EventoService, private teamState: TeamStateService) {}

  ngOnInit(): void {
    this.form = this.emptyForm();
    this.carregar();
  }

  private emptyForm(): EventoRequest {
    return { timeId: this.teamState.timeId!, nome: '', descricao: '', valorSugerido: undefined, dataInicio: '', dataFim: '' };
  }

  get listaFiltrada(): Evento[] {
    return this.eventos.filter(e => e.ativo === this.filtroAtivos);
  }

  get totalAtivos(): number { return this.eventos.filter(e => e.ativo).length; }
  get totalInativos(): number { return this.eventos.filter(e => !e.ativo).length; }

  formatData(d: string): string {
    return new Date(d + 'T00:00:00').toLocaleDateString('pt-BR');
  }

  carregar(): void {
    this.loading = true;
    this.erro = '';
    this.eventoService.findByTime(this.teamState.timeId!).subscribe({
      next: data => { this.eventos = data; this.loading = false; },
      error: () => { this.erro = 'Não foi possível carregar os eventos.'; this.loading = false; }
    });
  }

  abrirPanel(): void {
    this.form = this.emptyForm();
    this.showPanel = true;
    this.erro = '';
  }

  fecharPanel(): void { this.showPanel = false; }

  salvar(): void {
    if (!this.form.nome.trim() || !this.form.dataInicio || !this.form.dataFim) {
      this.erro = 'Preencha nome, data de início e data de fim.';
      return;
    }
    this.salvando = true;
    this.eventoService.create(this.form).subscribe({
      next: () => {
        this.fecharPanel();
        this.sucesso = 'Evento criado com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: () => { this.erro = 'Erro ao criar evento.'; this.salvando = false; }
    });
  }

  desativar(id: string): void {
    if (!confirm('Desativar este evento?')) return;
    this.eventoService.desativar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao desativar evento.'; }
    });
  }

  ativar(id: string): void {
    this.eventoService.ativar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao ativar evento.'; }
    });
  }
}
