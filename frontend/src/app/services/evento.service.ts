import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { Evento, EventoRequest } from '../models/evento.model';

@Injectable({ providedIn: 'root' })
export class EventoService {
  private api = `${API_URL}/api/evento/v1`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Evento[]> {
    return this.http.get<Evento[]>(this.api);
  }

  findByTime(timeId: string): Observable<Evento[]> {
    return this.http.get<Evento[]>(`${this.api}/time/${timeId}`);
  }

  create(evento: EventoRequest): Observable<Evento> {
    return this.http.post<Evento>(this.api, evento);
  }

  update(id: string, evento: Partial<EventoRequest>): Observable<Evento> {
    return this.http.patch<Evento>(`${this.api}/${id}`, evento);
  }

  ativar(id: string): Observable<Evento> {
    return this.http.patch<Evento>(`${this.api}/${id}/ativar`, {});
  }

  desativar(id: string): Observable<Evento> {
    return this.http.patch<Evento>(`${this.api}/${id}/desativar`, {});
  }
}
