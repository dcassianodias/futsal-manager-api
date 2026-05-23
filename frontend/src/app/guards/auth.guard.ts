import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TeamStateService } from '../services/team-state.service';

export const authGuard: CanActivateFn = () => {
  const state = inject(TeamStateService);
  const router = inject(Router);
  return state.timeId ? true : router.createUrlTree(['/login']);
};
