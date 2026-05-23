import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

// Configure em formspree.io → crie um formulário e substitua YOUR_FORM_ID
const FORMSPREE_URL = 'https://formspree.io/f/mlgvgoqy';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  email = '';
  enviando = false;
  enviado = false;
  erro = '';

  readonly features = [
    {
      titulo: 'Gestão de Atletas',
      desc: 'Perfil completo, posição e histórico de cada jogador do seu time',
      emBreve: false
    },
    {
      titulo: 'Mensalidades',
      desc: 'Geração automática e controle de quem pagou e quem está em débito',
      emBreve: false
    },
    {
      titulo: 'Jogos e Placar',
      desc: 'Agende partidas, registre placar, artilheiros e resultado final',
      emBreve: false
    },
    {
      titulo: 'Financeiro',
      desc: 'Controle de receitas, despesas e saldo do time em tempo real',
      emBreve: false
    },
    {
      titulo: 'Campeonatos',
      desc: 'Crie torneios com grupos, chaveamento e tabela de classificação',
      emBreve: true
    },
    {
      titulo: 'Notificações',
      desc: 'Atletas avisados no celular sobre jogos, mensalidades e eventos',
      emBreve: true
    },
  ];

  readonly passos = [
    {
      num: '01',
      titulo: 'Cadastre seu time',
      desc: 'Crie o perfil do time e adicione seus atletas em poucos minutos'
    },
    {
      num: '02',
      titulo: 'Gerencie tudo',
      desc: 'Jogos, mensalidades, despesas e desempenho num único painel'
    },
    {
      num: '03',
      titulo: 'Dispute campeonatos',
      desc: 'Crie ou participe de torneios com chaveamento automático'
    },
  ];

  constructor(private http: HttpClient) {}

  enviar(): void {
    if (!this.email.trim() || !this.email.includes('@')) {
      this.erro = 'Digite um e-mail válido.';
      return;
    }
    this.enviando = true;
    this.erro = '';
    this.http
      .post(FORMSPREE_URL, { email: this.email }, { headers: { Accept: 'application/json' } })
      .subscribe({
        next: () => { this.enviado = true; this.enviando = false; },
        error: () => { this.erro = 'Erro ao enviar. Tente novamente.'; this.enviando = false; }
      });
  }
}
