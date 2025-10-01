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
import { Carro } from '../models/carro';

export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
}

@Injectable({
  providedIn: 'root',
})
export class CarroService {
  http = inject(HttpClient);

  // ✅ URL base do backend
  private readonly API_URL = environment.apiUrl + '/carros';

  constructor() {}

  /**
   * Lista carros com paginação e ordenação.
   */
  listar(
    page: number = 0,
    size: number = 5,
    sortField: keyof Carro = 'id',
    sortDirection: 'asc' | 'desc' = 'asc',
    filtro?: string
  ): Observable<Paginacao<Carro>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    if (filtro) {
      params = params.set('filtro', filtro);
    }

    return this.http
      .get<Paginacao<Carro>>(this.API_URL, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Filtrar por placa, marca ou modelo (paginado).
   */
  filtrar(
    placa?: string,
    marca?: string,
    modelo?: string,
    page: number = 0,
    size: number = 5,
    sortField: keyof Carro = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Carro>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    if (placa) params = params.set('placa', placa);
    if (marca) params = params.set('marca', marca);
    if (modelo) params = params.set('modelo', modelo);

    return this.http
      .get<Paginacao<Carro>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar carro por ID (detalhado).
   */
  buscarPorId(id: number): Observable<Carro> {
    return this.http
      .get<Carro>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Cadastrar carro.
   */
  cadastrar(carro: Partial<Carro>): Observable<Carro> {
    return this.http
      .post<Carro>(this.API_URL, carro)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Atualizar carro.
   */
  atualizar(id: number, carro: Partial<Carro>): Observable<Carro> {
    return this.http
      .put<Carro>(`${this.API_URL}/${id}`, carro)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Excluir carro.
   */
  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Exportar relatórios.
   */
  exportar(tipo: 'pdf' | 'csv' | 'excel', filtro?: string): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtro) params = params.set('filtro', filtro);

    return this.http.get(`${this.API_URL}/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Tratamento centralizado de erros.
   */
  private tratarErro(error: HttpErrorResponse) {
    console.error('❌ Erro vindo do backend:', error);

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
