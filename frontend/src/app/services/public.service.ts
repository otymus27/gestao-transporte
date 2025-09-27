import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

export interface PastaPublica {
  id: number;
  nome: string;
  caminhoCompleto: string;
  subPastas: PastaPublica[];
  arquivos: ArquivoPublico[];
}

export interface PastaPublicaDTO {
  id: number;
  nomePasta: string;
  subPastas: PastaPublicaDTO[];
  arquivos: ArquivoPublicoDTO[];
}

export interface ArquivoPublicoDTO {
  id: number;
  nome: string;
  tamanho: number;
  dataAtualizacao: string;
}

export interface ArquivoPublico {
  id: number;
  nome: string;
  tamanho: number;
  tipoMime: string;
}

@Injectable({ providedIn: 'root' })
export class PublicService {
  // private apiUrl = 'http://localhost:8082/api/publico';

  private readonly apiUrl = `${environment.apiUrl}/publico`;

  constructor(private http: HttpClient) {}

  listarPastas(): Observable<PastaPublica[]> {
    return this.http.get<PastaPublica[]>(`${this.apiUrl}/pastas`);
  }

  listarArquivos(pastaId: number): Observable<ArquivoPublico[]> {
    return this.http.get<ArquivoPublico[]>(
      `${this.apiUrl}/visualizar/arquivo/${pastaId}`
    );
  }

  // public.service.ts
  listarArquivosDaFarmacia(): Observable<ArquivoPublicoDTO[]> {
    return this.http.get<ArquivoPublicoDTO[]>(`${this.apiUrl}/pastas/8`);
  }

  // Busca uma pasta publica por id
  getPastaPorId(id: number): Observable<PastaPublicaDTO> {
    return this.http.get<PastaPublicaDTO>(`${this.apiUrl}/pastas/${id}`);
  }

  getPastaFarmacia(): Observable<PastaPublicaDTO> {
    return this.http.get<PastaPublicaDTO>(`${this.apiUrl}/farmacia`);
  }

  visualizarArquivo(arquivoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/visualizar/arquivo/${arquivoId}`, {
      responseType: 'blob',
    });
  }

  downloadArquivo(arquivoId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/arquivo/${arquivoId}`, {
      responseType: 'blob',
    });
  }

  downloadPasta(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/pasta/${id}`, {
      responseType: 'blob',
    });
  }

  formatarTamanho(tamanhoBytes: number): string {
    if (tamanhoBytes < 1024) return `${tamanhoBytes} B`;
    const kb = tamanhoBytes / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;
    const mb = kb / 1024;
    if (mb < 1024) return `${mb.toFixed(1)} MB`;
    const gb = mb / 1024;
    return `${gb.toFixed(1)} GB`;
  }
}
