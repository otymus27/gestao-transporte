import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

/**
 * Filtros disponíveis para o relatório de Solicitações
 */
export interface FiltroSolicitacaoRelatorio {
  filtro?: string | null;
  dataInicio?: string | null; // yyyy-MM-dd
  dataFim?: string | null;    // yyyy-MM-dd
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioSolicitacaoService {
  private http = inject(HttpClient);

  // ✅ Endpoint base do módulo de Solicitação
  private readonly API_URL = environment.apiUrl + '/solicitacao';

  constructor() {}

  /**
   * Exporta relatório de Solicitações em PDF
   * GET /api/solicitacao/relatorio/pdf
   */
  exportarPdf(filtros: FiltroSolicitacaoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);

    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Exporta relatório de Solicitações em Excel (XLSX)
   * GET /api/solicitacao/relatorio/excel
   */
  exportarExcel(filtros: FiltroSolicitacaoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);

    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Monta os parâmetros da requisição conforme filtros informados
   */
  private buildParams(filtros: FiltroSolicitacaoRelatorio): HttpParams {
    let params = new HttpParams();

    if (filtros.filtro?.trim()) {
      params = params.set('filtro', filtros.filtro.trim());
    }

    if (filtros.dataInicio) {
      params = params.set('dataInicio', filtros.dataInicio);
    }

    if (filtros.dataFim) {
      params = params.set('dataFim', filtros.dataFim);
    }

    return params;
  }
}
