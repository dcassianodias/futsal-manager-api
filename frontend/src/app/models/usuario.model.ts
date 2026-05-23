export type PerfilUsuario = 'ADMIN' | 'ATLETA';

export interface Usuario {
  id: string;
  timeId: string;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
  gols?: number;
}

export interface UsuarioRequest {
  timeId: string;
  nome: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
}
