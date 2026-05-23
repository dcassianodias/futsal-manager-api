import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DespesaService } from '../../services/despesa.service';
import { TeamStateService } from '../../services/team-state.service';
import { Despesa, DespesaRequest, TipoDespesa } from '../../models/despesa.model';
import { fmt } from '../../config/constants';

@Component({
  selector: 'app-despesas',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './despesas.component.html',
  styleUrl: './despesas.component.css'
})
export class DespesasComponent implements OnInit {
  despesas: Despesa[] = [];
  loading = false;
  salvando = false;
  showPanel = false;
  erro = '';
  sucesso = '';
  fmt = fmt;

  form!: DespesaRequest;

  tiposDespesa: { value: TipoDespesa; label: string }[] = [
    { value: 'ALUGUEL_QUADRA', label: 'Aluguel de Quadra' },
    { value: 'UNIFORME', label: 'Uniforme' },
    { value: 'EVENTO', label: 'Evento' },
    { value: 'OUTROS', label: 'Outros' }
  ];

  constructor(private despesaService: DespesaService, private teamState: TeamStateService) {}

  ngOnInit(): void {
    this.form = this.emptyForm();
    this.carregar();
  }

  private emptyForm(): DespesaRequest {
    const hoje = new Date();
    const mes = `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-01`;
    return { timeId: this.teamState.timeId!, descricao: '', valor: 0, mesReferencia: mes, tipoDespesa: 'ALUGUEL_QUADRA' };
  }

  get totalGeral(): number {
    return this.despesas.reduce((s, d) => s + d.valor, 0);
  }

  get totalMesAtual(): number {
    const mes = new Date().toISOString().slice(0, 7);
    return this.despesas.filter(d => d.mesReferencia.slice(0, 7) === mes).reduce((s, d) => s + d.valor, 0);
  }

  tipoClass(t: TipoDespesa): string {
    const map: Record<TipoDespesa, string> = {
      ALUGUEL_QUADRA: 'badge badge-blue',
      UNIFORME: 'badge badge-purple',
      EVENTO: 'badge badge-amber',
      OUTROS: 'badge badge-gray'
    };
    return map[t];
  }

  tipoLabel(t: TipoDespesa): string {
    return this.tiposDespesa.find(x => x.value === t)?.label ?? t;
  }

  formatMes(d: string): string {
    const [year, month] = d.slice(0, 7).split('-');
    const meses = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    return `${meses[+month - 1]}/${year}`;
  }

  get mesInput(): string {
    return this.form.mesReferencia.slice(0, 7);
  }
  set mesInput(val: string) {
    this.form.mesReferencia = val ? `${val}-01` : '';
  }

  carregar(): void {
    this.loading = true;
    this.erro = '';
    this.despesaService.findByTime(this.teamState.timeId!).subscribe({
      next: data => {
        this.despesas = data.map(d => ({
          ...d,
          tipoDespesa: (d.tipoDespesa ?? (d as any).tipo) as any
        }));
        this.loading = false;
      },
      error: () => { this.erro = 'Não foi possível carregar as despesas.'; this.loading = false; }
    });
  }

  abrirPanel(): void {
    this.form = this.emptyForm();
    this.showPanel = true;
    this.erro = '';
  }

  fecharPanel(): void { this.showPanel = false; }

  salvar(): void {
    if (!this.form.descricao.trim() || !this.form.valor || this.form.valor <= 0 || !this.form.mesReferencia) {
      this.erro = 'Preencha descrição, valor e mês de referência.';
      return;
    }
    this.salvando = true;
    this.despesaService.create(this.form).subscribe({
      next: () => {
        this.fecharPanel();
        this.sucesso = 'Despesa registrada com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: () => { this.erro = 'Erro ao registrar despesa.'; this.salvando = false; }
    });
  }

  excluir(id: string): void {
    if (!confirm('Excluir esta despesa permanentemente?')) return;
    this.despesaService.delete(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao excluir despesa.'; }
    });
  }
}
