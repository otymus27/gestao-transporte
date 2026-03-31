import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RankingItem {
  nome: string;
  quantidade: number;
}

export interface SolicitacaoPorDia {
  data: string;
  total: number;
}

export interface Dashboard {
  totalUsuarios: number;
  totalCarros: number;
  totalMotoristas: number;
  totalSetores: number;
  totalSolicitacoes: number;
  solicitacoesEmAndamento: number;
  solicitacoesFinalizadas: number;
  solicitacoesCanceladas: number;
  usuariosAtivosAgora: number;
  usuariosLogaramHoje: number;
  solicitacoesPorDia: SolicitacaoPorDia[];
  topSetores: RankingItem[];
  topMotoristas: RankingItem[];
  // topCarros REMOVIDO — placa está na FichaSolicitacao
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/dashboard`;

  getDashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(this.API_URL);
  }
}
