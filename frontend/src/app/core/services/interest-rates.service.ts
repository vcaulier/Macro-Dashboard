import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { InterestRate } from '../../models/interest-rate.model';
import { ASSETS } from '../../models/asset.model';

@Injectable({
  providedIn: 'root'
})
export class InterestRatesService {
  private http = inject(HttpClient);
  private base = 'http://127.0.0.1:8081/api/interest-rates';
  
  private cache: InterestRate[] = [];
  private allRecords$ = new ReplaySubject<InterestRate[]>(1);

  loadAll(): Observable<InterestRate[]> {
    this.fetchAndCacheWithRetry();
    return this.allRecords$.asObservable();
  }

  getLatestRates(): InterestRate[] {
    const latestMap = new Map<string, InterestRate>(); 
    for (const asset of ASSETS) {
      const assetRates = this.cache.filter(element => element.asset === asset);
      const sorted = assetRates.sort((a, b) => a.date < b.date ? -1 : a.date > b.date ? 1 : 0);
      const latestRate = sorted[sorted.length - 1];
      latestMap.set(asset, latestRate);   
    }
    return Array.from(latestMap.values())
      .sort((a, b) => b.interestRate - a.interestRate);
  }

  getRateHistory(): InterestRate[] {
    return [...this.cache].sort((a, b) => a.date < b.date ? -1 : a.date > b.date ? 1 : 0);
  }

  private fetchAndCacheWithRetry(attempt = 1) {
    this.http.get<InterestRate[]>(`${this.base}`).subscribe({
      next: (data) => {
        if (!Array.isArray(data)) return;
        this.cache = data;
        this.allRecords$.next(data);
      },
      error: () => {
        if (attempt < 5) {
          setTimeout(() => this.fetchAndCacheWithRetry(attempt + 1), 15000);
        }
      }
    });
  }
}
