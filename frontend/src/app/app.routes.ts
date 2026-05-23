import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'times',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/times/times.component').then(m => m.TimesComponent)
  },
  {
    path: 'time/:id',
    loadComponent: () => import('./pages/time-publico/time-publico.component').then(m => m.TimePublicoComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'jogos',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/jogos/jogos.component').then(m => m.JogosComponent)
  },
  {
    path: 'jogadores',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/usuarios/usuarios.component').then(m => m.UsuariosComponent)
  },
  {
    path: 'pagamentos',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/pagamentos/pagamentos.component').then(m => m.PagamentosComponent)
  },
  {
    path: 'eventos',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/eventos/eventos.component').then(m => m.EventosComponent)
  },
  {
    path: 'despesas',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/despesas/despesas.component').then(m => m.DespesasComponent)
  }
];
