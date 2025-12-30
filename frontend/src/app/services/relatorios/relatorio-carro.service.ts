import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { FiltrosCarroRelatorio } from '../carro.service';

export interface FiltroCarroRelatorio {
  marca?: string | null;
  tipo?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioCarroService {
  http = inject(HttpClient);

  // ✅ Endpoint para a geração de relatórios

  // URL base da API (poderia ser movida para environment.ts)
  private readonly API_URL = environment.apiUrl + '/carros';

  constructor() {}

  exportarPdf(filtros: FiltrosCarroRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarExcel(filtros: FiltrosCarroRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private buildParams(filtros: FiltrosCarroRelatorio): HttpParams {
    let params = new HttpParams();
    if (filtros.placa?.trim())
      params = params.set('placa', filtros.placa.trim());
    if (filtros.marca?.trim())
      params = params.set('marca', filtros.marca.trim());
    if (filtros.modelo?.trim())
      params = params.set('modelo', filtros.modelo.trim());
    if (filtros.tipo?.trim()) params = params.set('tipo', filtros.tipo.trim());
    return params;
  }
}
