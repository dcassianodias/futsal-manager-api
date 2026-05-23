import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Time } from '../models/time.model';

@Injectable({ providedIn: 'root' })
export class TeamStateService {
  private readonly KEY = 'fm_team';
  private subject = new BehaviorSubject<Time | null>(this.load());

  team$ = this.subject.asObservable();
  get team(): Time | null { return this.subject.value; }
  get timeId(): string | null { return this.subject.value?.id ?? null; }

  set(time: Time): void {
    localStorage.setItem(this.KEY, JSON.stringify(time));
    this.subject.next(time);
  }

  clear(): void {
    localStorage.removeItem(this.KEY);
    this.subject.next(null);
  }

  private load(): Time | null {
    try {
      const s = localStorage.getItem(this.KEY);
      return s ? JSON.parse(s) : null;
    } catch { return null; }
  }
}
