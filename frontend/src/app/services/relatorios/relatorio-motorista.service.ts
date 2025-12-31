import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { FiltrosMotoristaRelatorio } from '../motorista.service';

export interface FiltroMotoristaRelatorio {
  nome?: string | null;
  ativo?: boolean | null;
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioMotoristaService {
  http = inject(HttpClient);

  // ✅ Endpoint para a geração de relatórios
  private readonly API_URL = environment.apiUrl + '/motorista';

  constructor() {}

  exportarPdf(filtros: FiltrosMotoristaRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  exportarExcel(filtros: FiltrosMotoristaRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private buildParams(filtros: FiltrosMotoristaRelatorio): HttpParams {
    let params = new HttpParams();

    if (filtros.matricula?.trim())
      params = params.set('matricula', filtros.matricula.trim());

    if (filtros.nome?.trim()) params = params.set('nome', filtros.nome.trim());    

    // ✅ Boolean precisa ser enviado como string "true"/"false"
    if (filtros.ativo !== null && filtros.ativo !== undefined) {
      params = params.set('ativo', String(filtros.ativo));
    }

    return params;
  }
}
