import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { Pagamento, PagamentoRequest, GerarMensalidadesRequest, GerarMensalidadeResponse } from '../models/pagamento.model';

@Injectable({ providedIn: 'root' })
export class PagamentoService {
  private api = `${API_URL}/api/pagamento/v1`;

  constructor(private http: HttpClient) {}

  findByTime(timeId: string): Observable<Pagamento[]> {
    return this.http.get<Pagamento[]>(`${this.api}/time/${timeId}`);
  }

  findPendentesByTime(timeId: string): Observable<Pagamento[]> {
    return this.http.get<Pagamento[]>(`${this.api}/time/${timeId}/pendentes`);
  }

  create(pagamento: PagamentoRequest): Observable<Pagamento> {
    return this.http.post<Pagamento>(this.api, pagamento);
  }

  pagar(id: string): Observable<Pagamento> {
    return this.http.patch<Pagamento>(`${this.api}/${id}/pagar`, {});
  }

  cancelar(id: string): Observable<Pagamento> {
    return this.http.patch<Pagamento>(`${this.api}/${id}/cancelar`, {});
  }

  gerarMensalidades(req: GerarMensalidadesRequest): Observable<GerarMensalidadeResponse> {
    return this.http.post<GerarMensalidadeResponse>(`${this.api}/mensalidades/gerar`, req);
  }
}
