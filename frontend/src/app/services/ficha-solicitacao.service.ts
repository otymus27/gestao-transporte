// src/app/services/ficha-solicitacao.service.ts
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Paginacao } from '../models/paginacao';
import {
  FichaSolicitacaoRequest,
  FichaSolicitacaoResponse,
} from '../models/ficha-solicitacao';

export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
}

@Injectable({ providedIn: 'root' })
export class FichaSolicitacaoService {
  private http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl + '/fichas';

  listar(
    page = 0,
    size = 5,
    sort = 'id,desc'
  ): Observable<Paginacao<FichaSolicitacaoResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    return this.http
      .get<Paginacao<FichaSolicitacaoResponse>>(this.API_URL, { params })
      .pipe(catchError(this.tratarErro));
  }

  buscarPorId(id: number): Observable<FichaSolicitacaoResponse> {
    return this.http
      .get<FichaSolicitacaoResponse>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  filtrar(filtros: {
    placa?: string;
    inicio?: string;
    fim?: string;
    usuarioId?: number;
  }, page = 0, size = 5): Observable<Paginacao<FichaSolicitacaoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filtros.placa?.trim())   params = params.set('placa', filtros.placa.trim());
    if (filtros.inicio)          params = params.set('inicio', filtros.inicio);
    if (filtros.fim)             params = params.set('fim', filtros.fim);
    if (filtros.usuarioId)       params = params.set('usuarioId', filtros.usuarioId);

    return this.http
      .get<Paginacao<FichaSolicitacaoResponse>>(`${this.API_URL}/buscar`, { params })
      .pipe(catchError(this.tratarErro));
  }

  salvar(ficha: FichaSolicitacaoRequest): Observable<FichaSolicitacaoResponse> {
    return this.http
      .post<FichaSolicitacaoResponse>(this.API_URL, ficha)
      .pipe(catchError(this.tratarErro));
  }

  atualizar(id: number, ficha: FichaSolicitacaoRequest): Observable<FichaSolicitacaoResponse> {
    return this.http
      .patch<FichaSolicitacaoResponse>(`${this.API_URL}/${id}`, ficha)
      .pipe(catchError(this.tratarErro));
  }

  excluir(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.tratarErro));
  }

  private tratarErro(error: HttpErrorResponse) {
    const b = error.error;
    return throwError(() => ({
      status:  b?.status  || error.status,
      error:   b?.erro    || b?.error   || error.statusText || 'Erro',
      message: b?.mensagem || b?.message || 'Erro ao processar requisição',
      path:    b?.path    || error.url  || '',
    } as ErrorMessage));
  }
}