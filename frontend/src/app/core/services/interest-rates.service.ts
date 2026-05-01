import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { InterestRate } from '../../models/interest-rate.model';

@Injectable({
  providedIn: 'root'
})
export class InterestRatesService {
  private http = inject(HttpClient);
  private base = 'http://127.0.0.1:8081/api/interest-rates';

  getRates(): Observable<InterestRate[]> {
    return this.http.get<InterestRate[]>(`${this.base}`).pipe(
      map(rates => [...rates].sort((a, b) => b.rate - a.rate)) // tri décroissant côté Angular
    );
  }
}
