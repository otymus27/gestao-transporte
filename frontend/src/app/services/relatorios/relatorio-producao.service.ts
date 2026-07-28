import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

/**
 * Filtros disponíveis para o relatório de Produção por Usuário
 * (quantidade de fichas e solicitações criadas por usuário no período).
 */
export interface FiltroProducaoRelatorio {
  nomeUsuario?: string | null;
  dataInicio?: string | null; // yyyy-MM-dd
  dataFim?: string | null; // yyyy-MM-dd
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioProducaoService {
  private http = inject(HttpClient);

  // ✅ Endpoint base do módulo de Produção
  private readonly API_URL = environment.apiUrl + '/producao';

  /**
   * Consulta paginada (para preencher a tabela no Angular antes de exportar)
   * GET /api/producao/relatorio/consultar
   */
  consultar(
    filtros: FiltroProducaoRelatorio,
    page: number,
    size: number
  ): Observable<any> {
    let params = this.buildParams(filtros)
      .set('page', page)
      .set('size', size);

    return this.http.get(`${this.API_URL}/relatorio/consultar`, { params });
  }

  /**
   * Exporta relatório de Produção por Usuário em PDF
   * GET /api/producao/relatorio/pdf
   */
  exportarPdf(filtros: FiltroProducaoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Exporta relatório de Produção por Usuário em Excel (XLSX)
   * GET /api/producao/relatorio/excel
   */
  exportarExcel(filtros: FiltroProducaoRelatorio): Observable<Blob> {
    const params = this.buildParams(filtros);
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  private buildParams(filtros: FiltroProducaoRelatorio): HttpParams {
    let params = new HttpParams();

    const nomeUsuario = filtros.nomeUsuario?.trim();
    if (nomeUsuario) {
      params = params.set('nomeUsuario', nomeUsuario);
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
