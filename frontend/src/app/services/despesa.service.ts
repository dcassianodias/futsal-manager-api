import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { Despesa, DespesaRequest } from '../models/despesa.model';

@Injectable({ providedIn: 'root' })
export class DespesaService {
  private api = `${API_URL}/api/despesa/v1`;

  constructor(private http: HttpClient) {}

  findByTime(timeId: string): Observable<Despesa[]> {
    return this.http.get<Despesa[]>(`${this.api}/time/${timeId}`);
  }

  create(despesa: DespesaRequest): Observable<Despesa> {
    return this.http.post<Despesa>(this.api, despesa);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
