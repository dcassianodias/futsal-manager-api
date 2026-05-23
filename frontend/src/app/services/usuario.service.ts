import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { Usuario, UsuarioRequest } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private api = `${API_URL}/api/usuario/v1`;

  constructor(private http: HttpClient) {}

  findByTime(timeId: string): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.api}/time/${timeId}`);
  }

  create(usuario: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(this.api, usuario);
  }

  update(id: string, usuario: Partial<UsuarioRequest>): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.api}/${id}`, usuario);
  }

  desativar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  reativar(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.api}/${id}/reativar`, {});
  }
}
