import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TimeService } from '../../services/time.service';
import { TeamStateService } from '../../services/team-state.service';
import { Time } from '../../models/time.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  times: Time[] = [];
  selectedId = '';
  loading = true;
  erro = '';

  constructor(
    private timeService: TimeService,
    private teamState: TeamStateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.timeService.findAll().subscribe({
      next: times => {
        this.times = times.filter(t => t.ativo);
        this.loading = false;
      },
      error: () => {
        this.erro = 'Não foi possível carregar os times. Verifique se o backend está rodando.';
        this.loading = false;
      }
    });
  }

  selecionar(id: string): void {
    this.selectedId = id;
  }

  entrar(): void {
    if (!this.selectedId) return;
    const time = this.times.find(t => t.id === this.selectedId);
    if (!time) return;
    this.teamState.set(time);
    this.router.navigate(['/dashboard']);
  }

  iniciais(nome: string): string {
    return nome.trim().split(/\s+/).slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }

  corAvatar(nome: string): string {
    const cores = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    return cores[Math.abs(hash) % cores.length];
  }
}
