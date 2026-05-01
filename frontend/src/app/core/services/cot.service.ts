import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CotNetData } from '../../models/cot.model';

@Injectable({
  providedIn: 'root'
})
export class CotService {

  private http = inject(HttpClient);
  private base = 'http://localhost:8081/api/cot-data';

  getCotData(): Observable<CotNetData[]> {
    return this.http.get<CotNetData[]>(`${this.base}`);
  }

}
