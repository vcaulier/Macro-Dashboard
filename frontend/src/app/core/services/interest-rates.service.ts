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
    return this.http.get<Record<string, number>>(`${this.base}`).pipe(
      map(response =>
        Object.entries(response)
          .map(([currency, rate]) => ({ currency, rate }))
          .sort((a, b) => b.rate - a.rate)
      )
    );
  }
}
