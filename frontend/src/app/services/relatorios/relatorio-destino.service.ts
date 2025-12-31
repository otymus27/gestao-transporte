import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

export interface FiltroDestinoRelatorio {
  nome?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioDestinoService {
  http = inject(HttpClient);

  // ✅ Endpoint base do módulo de Destino
  private readonly API_URL = environment.apiUrl + '/destino';

  constructor() {}

  exportarPdf(filtros: FiltroDestinoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarExcel(filtros: FiltroDestinoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private buildParams(filtros: FiltroDestinoRelatorio): HttpParams {
    let params = new HttpParams();

    if (filtros.nome?.trim()) {
      params = params.set('nome', filtros.nome.trim());
    }

    return params;
  }
}
