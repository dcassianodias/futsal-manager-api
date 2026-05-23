import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/constants';
import { Time, TimeCreateRequest, TimeUpdateRequest } from '../models/time.model';

@Injectable({ providedIn: 'root' })
export class TimeService {
  private api = `${API_URL}/api/time/v1`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Time[]> {
    return this.http.get<Time[]>(this.api);
  }

  findById(id: string): Observable<Time> {
    return this.http.get<Time>(`${this.api}/${id}`);
  }

  create(data: TimeCreateRequest): Observable<Time> {
    return this.http.post<Time>(this.api, data);
  }

  update(id: string, data: TimeUpdateRequest): Observable<Time> {
    return this.http.patch<Time>(`${this.api}/${id}`, data);
  }

  desativar(id: string): Observable<Time> {
    return this.http.patch<Time>(`${this.api}/${id}/desativar`, {});
  }

  ativar(id: string): Observable<Time> {
    return this.http.patch<Time>(`${this.api}/${id}/ativar`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
