import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TimeService } from '../../services/time.service';
import { Time } from '../../models/time.model';

@Component({
  selector: 'app-times',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './times.component.html',
  styleUrl: './times.component.css'
})
export class TimesComponent implements OnInit {
  times: Time[] = [];
  loading = true;
  erro = '';
  ano = new Date().getFullYear();

  constructor(private timeService: TimeService) {}

  ngOnInit(): void {
    this.timeService.findAll().subscribe({
      next: times => {
        this.times = times.filter(t => t.ativo);
        this.loading = false;
      },
      error: () => {
        this.erro = 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.';
        this.loading = false;
      }
    });
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
