import { Component, inject } from '@angular/core';
import { Router, NavigationEnd, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs/operators';
import { TeamStateService } from './services/team-state.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  private router = inject(Router);
  private teamState = inject(TeamStateService);

  isPublic = toSignal(
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd),
      map((e: NavigationEnd) => this.checkPublic(e.urlAfterRedirects)),
      startWith(this.checkPublic(this.router.url))
    ),
    { initialValue: this.checkPublic(this.router.url) }
  );

  team = toSignal(this.teamState.team$, { initialValue: this.teamState.team });

  iniciais(nome: string): string {
    return nome
      .split(' ')
      .filter(w => w.length > 0)
      .slice(0, 2)
      .map(w => w[0].toUpperCase())
      .join('');
  }

  corAvatar(nome: string): string {
    const cores = ['#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c', '#e67e22', '#34495e'];
    let hash = 0;
    for (let i = 0; i < nome.length; i++) hash = nome.charCodeAt(i) + ((hash << 5) - hash);
    return cores[Math.abs(hash) % cores.length];
  }

  sair(): void {
    this.teamState.clear();
    this.router.navigate(['/']);
  }

  private checkPublic(url: string): boolean {
    const noSidebarRoutes = ['/', '/login', '/times'];
    return noSidebarRoutes.includes(url) || url.startsWith('/time/');
  }
}
