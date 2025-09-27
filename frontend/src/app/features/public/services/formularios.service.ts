// src/app/services/formularios.service.ts
import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../../../environments/environment.prod';

// ============================
// DTOs Públicos
// ============================
export interface PastaExplorerDTO {
  id: number;
  nomePasta: string;
  subPastas?: PastaExplorerDTO[];
  formularios?: FormularioDTO[];
}

export interface PastaFormularioDTO {
  id: number;
  nomePasta: string;
  caminhoCompleto: string;
  quantidadeArquivos: number;
  tamanhoTotal: number;
}

export interface FormularioDTO {
  id: number;
  nomeArquivo: string;
  caminhoArmazenamento?: string;
  tamanho: number;
  dataUpload: string;
}

export interface UploadFormularioResponse {
  id: number;
  nomeArquivo: string;
  pastaId: number;
  nomePasta: string;
  tamanho: number;
  dataUpload: string;
}

// ✅ Interface de erros do backend
export interface ErrorMessage {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp?: string;
}

@Injectable({ providedIn: 'root' })
export class FormulariosService {
  //private readonly apiUrlPublic = 'http://localhost:8082/api/public';
  //private readonly apiUrlAdmin = 'http://localhost:8082/api/admin';

  private readonly apiUrlPublic = `${environment.apiUrl}/public`;
  private readonly apiUrlAdmin = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient, private router: Router) {}

  // ===============================
  // 📂 Endpoints Públicos
  // ===============================

  listarExplorer(
    sortBy: string = 'nomeArquivo',
    order: 'asc' | 'desc' = 'asc'
  ): Observable<PastaExplorerDTO[]> {
    const params = new HttpParams().set('sortBy', sortBy).set('order', order);
    return this.http
      .get<PastaExplorerDTO[]>(`${this.apiUrlPublic}/explorer`, { params })
      .pipe(catchError(this.tratarErro));
  }

  listarPastasPublicas(): Observable<PastaFormularioDTO[]> {
    return this.http
      .get<PastaFormularioDTO[]>(`${this.apiUrlPublic}/pastas`)
      .pipe(catchError(this.tratarErro));
  }

  listarFormularios(
    pastaId: number,
    sortBy: string = 'nomeArquivo',
    order: 'asc' | 'desc' = 'asc'
  ): Observable<FormularioDTO[]> {
    return this.http
      .get<FormularioDTO[]>(
        `${this.apiUrlPublic}/pastas/${pastaId}/formularios`,
        {
          params: { sortBy, order },
        }
      )
      .pipe(catchError(this.tratarErro));
  }

  downloadFormulario(
    id: number,
    modo: 'download' | 'inline' = 'download'
  ): Observable<Blob> {
    const params = new HttpParams().set('modo', modo);
    return this.http
      .get(`${this.apiUrlPublic}/formularios/${id}/download`, {
        params,
        responseType: 'blob',
      })
      .pipe(catchError(this.tratarErro));
  }

  // ===============================
  // 🔒 Endpoints Administrativos
  // ===============================

  criarPasta(nomePasta: string): Observable<PastaFormularioDTO> {
    return this.http
      .post<PastaFormularioDTO>(`${this.apiUrlAdmin}/pastas`, { nomePasta })
      .pipe(catchError(this.tratarErro));
  }

  renomearPasta(
    pastaId: number,
    novoNome: string
  ): Observable<PastaFormularioDTO> {
    return this.http
      .put<PastaFormularioDTO>(`${this.apiUrlAdmin}/pastas/${pastaId}`, {
        novoNome,
      })
      .pipe(catchError(this.tratarErro));
  }

  renomearFormulario(
    formularioId: number,
    novoNome: string
  ): Observable<FormularioDTO> {
    return this.http
      .put<FormularioDTO>(
        `${this.apiUrlAdmin}/formularios/${formularioId}/renomear`,
        {
          novoNome,
        }
      )
      .pipe(catchError(this.tratarErro));
  }

  excluirPasta(pastaId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrlAdmin}/pastas/${pastaId}`)
      .pipe(catchError(this.tratarErro));
  }

  uploadFormulario(
    pastaId: number,
    file: File
  ): Observable<UploadFormularioResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<UploadFormularioResponse>(
        `${this.apiUrlAdmin}/pastas/${pastaId}/upload`,
        formData
      )
      .pipe(catchError(this.tratarErro));
  }

  uploadMultiplos(
    pastaId: number,
    files: File[]
  ): Observable<UploadFormularioResponse[]> {
    const formData = new FormData();
    files.forEach((f) => formData.append('files', f));
    return this.http
      .post<UploadFormularioResponse[]>(
        `${this.apiUrlAdmin}/pastas/${pastaId}/upload-multiplo`,
        formData
      )
      .pipe(catchError(this.tratarErro));
  }

  substituirFormulario(
    formularioId: number,
    file: File
  ): Observable<UploadFormularioResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .put<UploadFormularioResponse>(
        `${this.apiUrlAdmin}/formularios/${formularioId}/substituir`,
        formData
      )
      .pipe(catchError(this.tratarErro));
  }

  excluirFormulario(formularioId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrlAdmin}/formularios/${formularioId}`)
      .pipe(catchError(this.tratarErro));
  }

  excluirVarios(ids: number[]): Observable<void> {
    return this.http
      .request<void>('delete', `${this.apiUrlAdmin}/formularios/excluir-lote`, {
        body: ids,
      })
      .pipe(catchError(this.tratarErro));
  }

  // ===============================
  // ⚠️ Tratamento centralizado de erros
  // ===============================
  private tratarErro(error: HttpErrorResponse) {
    console.error('Erro capturado no AdminService:', error);
  
    // ✅ Caso especial para uploads muito grandes
    if (error.status === 413) {
      const erroBackend: ErrorMessage = {
        status: 413,
        error: 'Arquivo muito grande',
        message: 'O arquivo enviado excede o limite permitido pelo servidor.',
        path: error.url || '',
        timestamp: new Date().toISOString(),
      };
      return throwError(() => erroBackend);
    }
  
    // 🔹 Demais casos (fallback)
    const erroBackend: ErrorMessage = {
      status: error.status,
      error: error.error?.error || 'Erro desconhecido',
      message: error.error?.message || 'Erro ao processar requisição',
      path: error.error?.path || error.url || '',
      timestamp: error.error?.timestamp || new Date().toISOString(),
    };
  
    return throwError(() => erroBackend);
  }
  
  // ===============================
  // 📐 Utilitário para exibir tamanho legível
  // ===============================
  formatarTamanho(bytes: number): string {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
