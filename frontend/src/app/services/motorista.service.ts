// src/app/services/motorista.service.ts
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
import { Motorista } from '../models/motorista';

import { of, map } from 'rxjs';

export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
}

@Injectable({
  providedIn: 'root',
})
export class MotoristaService {
  http = inject(HttpClient);

  private readonly API_URL = environment.apiUrl + '/motorista';

  constructor() {}

  /**
   * Lista motoristas com paginação e ordenação.
   */
  listar(
    page: number = 0,
    size: number = 5,
    sortField: keyof Motorista = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Motorista>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    return this.http
      .get<Paginacao<Motorista>>(this.API_URL, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Filtrar motoristas por nome e/ou matrícula (paginado).
   */
  filtrar(
    nome?: string,
    matricula?: string,
    page: number = 0,
    size: number = 5,
    sortField: keyof Motorista = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Motorista>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    if (nome) params = params.set('nome', nome);
    if (matricula) params = params.set('matricula', matricula);

    return this.http
      .get<Paginacao<Motorista>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar motoristas para o combobox (matrícula ou nome, paginado).
   */
  buscarParaCombo(
    termo: string,
    page: number = 0,
    size: number = 10
  ): Observable<Motorista[]> {
    if (!termo.trim()) {
      return of([]); // precisa importar 'of' do rxjs
    }

    let params = new HttpParams()
      .set('termo', termo.trim())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http
      .get<Paginacao<Motorista>>(`${this.API_URL}/buscar-combo`, { params })
      .pipe(
        map((res) => res.content), // pega só o content pra popular o combo
        catchError(this.tratarErro)
      );
  }

  /**
   * Buscar motoristas filtrando por nome (paginado).
   */
  filtrarPorNome(
    nome: string,
    page: number = 0,
    size: number = 10,
    sortField: keyof Motorista = 'nome',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): Observable<Paginacao<Motorista>> {
    let params = new HttpParams()
      .set('nome', nome)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', `${sortField},${sortDirection}`);

    return this.http
      .get<Paginacao<Motorista>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar motorista por ID.
   */
  buscarPorId(id: number): Observable<Motorista> {
    return this.http
      .get<Motorista>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Buscar motorista detalhado.
   */
  buscarDetalhado(id: number): Observable<Motorista> {
    return this.http
      .get<Motorista>(`${this.API_URL}/detalhado/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Cadastrar motorista.
   */
  cadastrar(motorista: Partial<Motorista>): Observable<Motorista> {
    return this.http
      .post<Motorista>(this.API_URL, motorista)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Atualizar motorista.
   */
  atualizar(id: number, motorista: Partial<Motorista>): Observable<Motorista> {
    return this.http
      .put<Motorista>(`${this.API_URL}/${id}`, motorista)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Excluir motorista.
   */
  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  /**
   * Exportação de relatórios.
   */
  exportarExcel(nome?: string, matricula?: string): Observable<Blob> {
    let params = new HttpParams();
    if (nome) params = params.set('nome', nome);
    if (matricula) params = params.set('matricula', matricula);

    return this.http.get(`${this.API_URL}/relatorio`, {
      params: params.set('tipo', 'excel'),
      responseType: 'blob',
    });
  }

  exportarCsv(nome?: string, matricula?: string): Observable<Blob> {
    let params = new HttpParams();
    if (nome) params = params.set('nome', nome);
    if (matricula) params = params.set('matricula', matricula);

    return this.http.get(`${this.API_URL}/relatorio`, {
      params: params.set('tipo', 'csv'),
      responseType: 'blob',
    });
  }

  exportarPdf(nome?: string, matricula?: string): Observable<Blob> {
    let params = new HttpParams();
    if (nome) params = params.set('nome', nome);
    if (matricula) params = params.set('matricula', matricula);

    return this.http.get(`${this.API_URL}/relatorio`, {
      params: params.set('tipo', 'pdf'),
      responseType: 'blob',
    });
  }

  /**
   * Tratamento centralizado de erros.
   */
  private tratarErro(error: HttpErrorResponse) {
    console.error('Erro no backend Motorista:', error);

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
