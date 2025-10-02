import { inject, Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Paginacao } from '../models/paginacao';
import { environment } from '../../environments/environment';
import { Solicitacao } from '../models/solicitacao';

export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
}

@Injectable({
  providedIn: 'root',
})
export class SolicitacaoService {
  http = inject(HttpClient);

  // ✅ URL base
  private readonly API_URL = environment.apiUrl + '/solicitacao';

  constructor() {}

  /**
   * Listar solicitações com paginação
   */
  listar(
    page: number = 0,
    size: number = 5,
    sortField: keyof Solicitacao = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Solicitacao>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    return this.http
      .get<Paginacao<Solicitacao>>(this.API_URL, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar por ID resumido
   */
  buscarPorId(id: number): Observable<Solicitacao> {
    return this.http
      .get<Solicitacao>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar detalhado
   */
  buscarDetalhado(id: number): Observable<Solicitacao> {
    return this.http
      .get<Solicitacao>(`${this.API_URL}/detalhada/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Filtros específicos
   */
  filtrarPorStatus(
    status: string,
    page = 0,
    size = 5
  ): Observable<Paginacao<Solicitacao>> {
    let params = new HttpParams()
      .set('status', status)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http
      .get<Paginacao<Solicitacao>>(`${this.API_URL}/buscar/status`, { params })
      .pipe(catchError(this.tratarErro));
  }

  filtrarGenerico(
    filtros: {
      id?: number;
      status?: string;
      motoristaId?: number;
      carroId?: number;
      setorId?: number;
      usuarioId?: number;
      destinoId?: number;
      usuarioNome?: string;
      username?: string;
    },
    page = 0,
    size = 5
  ): Observable<Paginacao<Solicitacao>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    Object.keys(filtros).forEach((key) => {
      const valor = (filtros as any)[key];
      if (valor !== undefined && valor !== null) {
        params = params.set(key, valor.toString());
      }
    });

    return this.http
      .get<Paginacao<Solicitacao>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * CRUD
   */
  cadastrar(dto: Partial<Solicitacao>): Observable<Solicitacao> {
    return this.http
      .post<Solicitacao>(this.API_URL, dto)
      .pipe(catchError(this.tratarErro));
  }

  atualizar(id: number, dto: Partial<Solicitacao>): Observable<Solicitacao> {
    return this.http
      .patch<Solicitacao>(`${this.API_URL}/${id}`, dto)
      .pipe(catchError(this.tratarErro));
  }

  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Relatórios
   */
  exportar(tipo: 'excel' | 'csv' | 'pdf', filtro?: string): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtro) params = params.set('filtro', filtro);

    return this.http.get(`${this.API_URL}/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Tratamento de erro
   */
  private tratarErro(error: HttpErrorResponse) {
    console.error('Erro backend (SolicitacaoService):', error);

    const backendError = error.error;
    const errMsg: ErrorMessage = {
      status: backendError?.status || error.status,
      error:
        backendError?.erro || backendError?.error || error.statusText || 'Erro',
      message:
        backendError?.mensagem ||
        backendError?.message ||
        'Erro ao processar requisição',
      path: backendError?.path || error.url || '',
    };

    return throwError(() => errMsg);
  }
}
