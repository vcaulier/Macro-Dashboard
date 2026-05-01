import { Injectable, OnDestroy, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, ReplaySubject } from 'rxjs';
import { CotNetData } from '../../models/cot.model';
import { Asset, ASSETS } from '../../models/asset.model';

@Injectable({
  providedIn: 'root'
})
export class CotService implements OnDestroy {

  private http = inject(HttpClient);
  private base = 'http://127.0.0.1:8081/api/cot-data';
  
  private cache = new Map<Asset, CotNetData[]>();
  private allRecords$ = new ReplaySubject<CotNetData[]>(1);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  private readonly REFRESH_INTERVAL_MS = 60 * 60 * 1000;

  loadAll(): Observable<CotNetData[]> {
    this.fetchAndCacheWithRetry();
    this.startCron();
    return this.allRecords$.asObservable();
  }

  getByAsset(asset: Asset): CotNetData[] {
    return this.cache.get(asset) ?? [];
  }

  private fetchAndCacheWithRetry(attempt = 1) {
    this.http.get<CotNetData[]>(`${this.base}`).subscribe({
      next: (data) => {
        if (!Array.isArray(data))
          return;
        ASSETS.forEach(asset => {
          this.cache.set(asset, data.filter(d => d.asset === asset));
        });
        this.allRecords$.next(data);
      },
      error: () => {
        if (attempt < 5) {
          // Réessaie dans 15s si échec
          setTimeout(() => this.fetchAndCacheWithRetry(attempt + 1), 15000);
        }
      }});
  }

  private startCron() {
    if (this.refreshInterval) return; // déjà démarré
    this.refreshInterval = setInterval(() => {
      this.fetchAndCacheWithRetry();
    }, this.REFRESH_INTERVAL_MS);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

}
