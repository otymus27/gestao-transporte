// src/app/models/setor.ts

// import { SolicitacaoResumo } from './solicitacao'; // caso já exista no seu projeto

/**
 * Representa um Setor no sistema
 */
export interface Setor {
  id: number;
  nome: string;

  // Apenas no caso detalhado (se precisar usar em SetorDetalhadoDTO)
  // solicitacoes?: SolicitacaoResumo[];
}

/**
 * Representa um Setor resumido (ex.: ResponseDTO)
 */
export interface SetorResponse {
  id: number;
  nome: string;
}

/**
 * Representa a requisição de cadastro/atualização de setor
 */
export interface SetorRequest {
  nome: string;
}
