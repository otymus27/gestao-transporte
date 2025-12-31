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
import { Destino } from '../models/destino';

export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
}

@Injectable({
  providedIn: 'root',
})
export class DestinoService {
  http = inject(HttpClient);

  // ✅ URL base do backend
  private readonly API_URL = environment.apiUrl + '/destino';

  constructor() {}

  /**
   * Lista destinos com paginação e ordenação.
   */
  listar(
    page: number = 0,
    size: number = 5,
    sortField: keyof Destino = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Destino>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    return this.http
      .get<Paginacao<Destino>>(this.API_URL, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar destinos filtrando por nome (paginado).
   */
  filtrarPorNome(
    nome: string,
    page: number = 0,
    size: number = 5,
    sortField: keyof Destino = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Destino>> {
    let params = new HttpParams()
      .set('nome', nome)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    return this.http
      .get<Paginacao<Destino>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Filtrar por nome para gerar relatórios de setores
   */
  consultarParaRelatorio(
    filtros: { nome?: string | null },
    page: number,
    size: number
  ): Observable<Paginacao<any[]>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filtros.nome?.trim()) {
      params = params.set('nome', filtros.nome.trim());
    }

    // Exemplo de endpoint:
    // GET /setor/relatorio/consultar?nome=...
    return this.http.get<Paginacao<any[]>>(
      `${this.API_URL}/relatorio/consultar`,
      { params }
    );
  }

  /**
   * Busca destino por ID (resumido).
   */
  buscarPorId(id: number): Observable<Destino> {
    return this.http
      .get<Destino>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Busca destino detalhado.
   */
  buscarDetalhado(id: number): Observable<Destino> {
    return this.http
      .get<Destino>(`${this.API_URL}/detalhado/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Cadastra um novo destino.
   */
  cadastrar(destino: Partial<Destino>): Observable<Destino> {
    return this.http
      .post<Destino>(this.API_URL, destino)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Atualiza um destino existente.
   */
  atualizar(id: number, destino: Partial<Destino>): Observable<Destino> {
    return this.http
      .patch<Destino>(`${this.API_URL}/${id}`, destino)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Exclui um destino pelo ID.
   */
  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Exportar relatórios.
   */
  exportarExcel(filtro?: string): Observable<Blob> {
    let params = filtro ? new HttpParams().set('filtro', filtro) : undefined;
    return this.http.get(`${this.API_URL}/relatorio/excel`, {
      params,
      responseType: 'blob',
    });
  }

  exportarCsv(filtro?: string): Observable<Blob> {
    let params = filtro ? new HttpParams().set('filtro', filtro) : undefined;
    return this.http.get(`${this.API_URL}/relatorio/csv`, {
      params,
      responseType: 'blob',
    });
  }

  exportarPdf(filtro?: string): Observable<Blob> {
    let params = filtro ? new HttpParams().set('filtro', filtro) : undefined;
    return this.http.get(`${this.API_URL}/relatorio/pdf`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Exportar relatório unificado
   */
  exportarRelatorio(
    tipo: 'pdf' | 'csv' | 'excel' = 'pdf',
    filtro?: string
  ): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo).set('filtro', filtro || '');

    return this.http.get(`${this.API_URL}/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Tratamento centralizado de erros.
   */
  private tratarErro(error: HttpErrorResponse) {
    console.error('Ocorreu um erro vindo do backend:', error);

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

    console.warn('📌 Objeto de erro padronizado:', errMsg);

    return throwError(() => errMsg);
  }
}
