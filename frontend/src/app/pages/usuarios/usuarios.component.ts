import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../../services/usuario.service';
import { TeamStateService } from '../../services/team-state.service';
import { Usuario, UsuarioRequest, PerfilUsuario } from '../../models/usuario.model';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  loading = false;
  salvando = false;
  showPanel = false;
  filtroAtivos = true;
  erro = '';
  sucesso = '';

  form!: UsuarioRequest;

  constructor(private usuarioService: UsuarioService, private teamState: TeamStateService) {}

  ngOnInit(): void {
    this.form = this.emptyForm();
    this.carregar();
  }

  private emptyForm(): UsuarioRequest {
    return { timeId: this.teamState.timeId!, nome: '', email: '', senha: '', perfil: 'ATLETA' };
  }

  get listaFiltrada(): Usuario[] {
    return this.usuarios.filter(u => u.ativo === this.filtroAtivos);
  }

  get totalAtivos(): number { return this.usuarios.filter(u => u.ativo).length; }
  get totalInativos(): number { return this.usuarios.filter(u => !u.ativo).length; }

  perfilClass(p: PerfilUsuario): string {
    return p === 'ADMIN' ? 'badge badge-purple' : 'badge badge-blue';
  }

  perfilLabel(p: PerfilUsuario): string {
    return p === 'ADMIN' ? 'Admin' : 'Atleta';
  }

  carregar(): void {
    this.loading = true;
    this.erro = '';
    this.usuarioService.findByTime(this.teamState.timeId!).subscribe({
      next: data => { this.usuarios = data; this.loading = false; },
      error: () => { this.erro = 'Não foi possível carregar os jogadores.'; this.loading = false; }
    });
  }

  abrirPanel(): void {
    this.form = this.emptyForm();
    this.showPanel = true;
    this.erro = '';
  }

  fecharPanel(): void { this.showPanel = false; }

  salvar(): void {
    if (!this.form.nome.trim() || !this.form.email.trim() || !this.form.senha.trim()) {
      this.erro = 'Preencha todos os campos obrigatórios.';
      return;
    }
    this.salvando = true;
    this.usuarioService.create(this.form).subscribe({
      next: () => {
        this.fecharPanel();
        this.sucesso = 'Jogador adicionado com sucesso!';
        this.carregar();
        this.salvando = false;
        setTimeout(() => this.sucesso = '', 3000);
      },
      error: (err) => {
        this.erro = err.status === 409 ? 'Este e-mail já está cadastrado no time.' : 'Erro ao cadastrar jogador.';
        this.salvando = false;
      }
    });
  }

  desativar(id: string): void {
    if (!confirm('Desativar este jogador?')) return;
    this.filtroAtivos = false;
    this.usuarioService.desativar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.filtroAtivos = true; this.erro = 'Erro ao desativar jogador.'; }
    });
  }

  reativar(id: string): void {
    this.usuarioService.reativar(id).subscribe({
      next: () => this.carregar(),
      error: () => { this.erro = 'Erro ao reativar jogador.'; }
    });
  }
}
