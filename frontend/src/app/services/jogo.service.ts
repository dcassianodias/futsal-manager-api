import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { FinalizarJogoRequest, Jogo, JogoRequest } from '../models/jogo.model';

@Injectable({ providedIn: 'root' })
export class JogoService {
  private api = `${API_URL}/api/jogo/v1`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Jogo[]> {
    return this.http.get<Jogo[]>(this.api);
  }

  findByTime(timeId: string): Observable<Jogo[]> {
    return this.http.get<Jogo[]>(`${this.api}/time/${timeId}`);
  }

  create(jogo: JogoRequest): Observable<Jogo> {
    return this.http.post<Jogo>(this.api, jogo);
  }

  update(id: string, jogo: Partial<JogoRequest>): Observable<Jogo> {
    return this.http.patch<Jogo>(`${this.api}/${id}`, jogo);
  }

  finalizar(id: string, req: FinalizarJogoRequest): Observable<Jogo> {
    return this.http.patch<Jogo>(`${this.api}/${id}/finalizar`, req);
  }

  cancelar(id: string): Observable<Jogo> {
    return this.http.patch<Jogo>(`${this.api}/${id}/cancelar`, {});
  }
}
