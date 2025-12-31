import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

export interface FiltroSetorRelatorio {
  nome?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioSetorService {
  http = inject(HttpClient);

  // ✅ Endpoint base do módulo de Setor
  private readonly API_URL = environment.apiUrl + '/setor';

  constructor() {}

  exportarPdf(filtros: FiltroSetorRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarExcel(filtros: FiltroSetorRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private buildParams(filtros: FiltroSetorRelatorio): HttpParams {
    let params = new HttpParams();

    if (filtros.nome?.trim()) {
      params = params.set('nome', filtros.nome.trim());
    }

    return params;
  }
}
