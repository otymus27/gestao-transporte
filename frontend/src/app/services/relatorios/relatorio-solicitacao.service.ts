import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

/**
 * Tipos aceitos pelo backend (enum TipoRelatorioSolicitacao)
 */
export type TipoRelatorioSolicitacao =
  | 'SIMPLES'
  | 'POR_SETOR'
  | 'POR_MOTORISTA'
  | 'POR_CARRO'
  | 'POR_DESTINO'
  | 'POR_USUARIO';

/**
 * Filtros disponíveis para o relatório de Solicitações
 */
export interface FiltroSolicitacaoRelatorio {
  tipo?: TipoRelatorioSolicitacao | null; // ✅ novo (agrupamento)
  filtro?: string | null;
  dataInicio?: string | null; // yyyy-MM-dd
  dataFim?: string | null; // yyyy-MM-dd
}

@Injectable({
  providedIn: 'root',
})
export class RelatorioSolicitacaoService {
  private http = inject(HttpClient);

  // ✅ Endpoint base do módulo de Solicitação
  private readonly API_URL = environment.apiUrl + '/solicitacao';

  /**
   * ✅ Tipo padrão no FRONT:
   * como este service nasceu para "relatório simples", o default é SEM_AGRUPAMENTO.
   * (se preferir deixar padrão POR_SETOR, é só trocar aqui)
   */
  private readonly DEFAULT_TIPO: TipoRelatorioSolicitacao = 'SIMPLES';

  constructor() {}

  /**
   * Exporta relatório de Solicitações em PDF
   * GET /api/solicitacao/relatorio/pdf?tipo=...
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
   * GET /api/solicitacao/relatorio/excel?tipo=...
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
   * ✅ inclui o tipo (agrupamento)
   */
  private buildParams(filtros: FiltroSolicitacaoRelatorio): HttpParams {
    let params = new HttpParams();

    // ✅ tipo (se não vier, aplica padrão SEM_AGRUPAMENTO)
    const tipo = (filtros.tipo ??
      this.DEFAULT_TIPO) as TipoRelatorioSolicitacao;
    params = params.set('tipo', tipo);

    // filtro
    const filtroTrim = filtros.filtro?.trim();
    if (filtroTrim) {
      params = params.set('filtro', filtroTrim);
    }

    // datas
    if (filtros.dataInicio) {
      params = params.set('dataInicio', filtros.dataInicio);
    }

    if (filtros.dataFim) {
      params = params.set('dataFim', filtros.dataFim);
    }

    return params;
  }
}
