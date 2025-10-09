import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class RelatorioService {
  http = inject(HttpClient);

  // ✅ Endpoint para a geração de relatórios

  // URL base da API (poderia ser movida para environment.ts)
  private readonly API_URL = environment.apiUrl;

  constructor() {}

  // 🔹 Carros
  listarCarros(filtros: any): Observable<any> {
    let params = new HttpParams();
    Object.keys(filtros).forEach((key) => {
      if (filtros[key]) params = params.set(key, filtros[key]);
    });
    return this.http.get(`${this.API_URL}/carros/buscar`, { params });
  }

  exportarCarros(tipo: string, filtroGlobal?: string): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtroGlobal) params = params.set('filtro', filtroGlobal);

    return this.http.get(`${this.API_URL}/carros/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  // 🔹 Destinos
  listarDestinos(filtros: any): Observable<any> {
    let params = new HttpParams();
    Object.keys(filtros).forEach((key) => {
      if (filtros[key]) params = params.set(key, filtros[key]);
    });
    return this.http.get(`${this.API_URL}/destino/buscar`, { params });
  }

  exportarDestinos(tipo: string, filtros: any): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    Object.keys(filtros).forEach((key) => {
      if (filtros[key]) params = params.set(key, filtros[key]);
    });

    return this.http.get(`${this.API_URL}/destino/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  // 🔹 Motoristas
  listarMotoristas(filtros: any): Observable<any> {
    let params = new HttpParams();
    Object.keys(filtros).forEach((key) => {
      if (filtros[key]) params = params.set(key, filtros[key]);
    });
    return this.http.get(`${this.API_URL}/motorista/buscar`, { params });
  }

  exportarMotoristas(tipo: string, filtros: any): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtros.nome) params = params.set('nome', filtros.nome);
    if (filtros.matricula) params = params.set('matricula', filtros.matricula);

    return this.http.get(`${this.API_URL}/motorista/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  // 🔹 Setores
  listarSetores(filtros: any): Observable<any> {
    let params = new HttpParams();
    if (filtros.nome) params = params.set('nome', filtros.nome);
    return this.http.get(`${this.API_URL}/setor/buscar`, { params });
  }

  exportarSetores(tipo: string, filtro?: string): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtro) params = params.set('filtro', filtro);

    return this.http.get(`${this.API_URL}/setor/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  // 🔹 Solicitações
  listarSolicitacoes(filtros: any): Observable<any> {
    let params = new HttpParams();
    Object.keys(filtros).forEach((key) => {
      if (
        filtros[key] !== null &&
        filtros[key] !== undefined &&
        filtros[key] !== ''
      ) {
        params = params.set(key, filtros[key]);
      }
    });

    return this.http.get(`${this.API_URL}/solicitacao/buscar`, { params });
  }

  exportarSolicitacoes(tipo: string, filtroGlobal?: string): Observable<Blob> {
    let params = new HttpParams().set('tipo', tipo);
    if (filtroGlobal) params = params.set('filtro', filtroGlobal);

    return this.http.get(`${this.API_URL}/solicitacao/relatorio`, {
      params,
      responseType: 'blob',
    });
  }

  // ==========================================================
  // 🔹 LISTAS AUXILIARES PARA COMBOBOX / AUTOCOMPLETE
  // ==========================================================
  listarTodosMotoristas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/motorista/buscar`);
  }

  listarTodosCarros(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/carros/buscar`);
  }

  listarTodosSetores(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/setor/buscar`);
  }

  listarTodosDestinos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/destino/buscar`);
  }
}
