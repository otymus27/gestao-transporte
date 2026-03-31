// src/app/models/ficha-solicitacao.ts

export interface SolicitacaoItem {
  id?: number;
  motoristaId: number | null;
  motoristaNome?: string;
  setorId: number | null;
  setorNome?: string;
  destinoId: number | null;
  destinoNome?: string;
  kmInicial: number | null;
  kmFinal: number | null;
  horaSaida: string | null; // HH:mm
  horaChegada: string | null; // HH:mm
  status: string;
  dataSolicitacao?: string;
}

export interface FichaSolicitacaoRequest {
  dataViagem: string; // yyyy-MM-dd
  placaVeiculo: string;
  solicitacoes: SolicitacaoItem[];
}

export interface FichaSolicitacaoResponse {
  id: number;
  dataViagem: string;
  placaVeiculo: string;
  dataCriacao: string;
  dataAtualizacao?: string;
  usuarioId: number;
  usuarioNome: string;
  usuarioUsername: string;
  solicitacoes: SolicitacaoItem[];
}

export function novaFicha(): FichaSolicitacaoRequest {
  return {
    dataViagem: new Date().toISOString().slice(0, 10),
    placaVeiculo: '',
    solicitacoes: [],
  };
}

export function novaSolicitacaoItem(): SolicitacaoItem {
  return {
    motoristaId: null,
    setorId: null,
    destinoId: null,
    kmInicial: null,
    kmFinal: null,
    horaSaida: null,
    horaChegada: null,
    status: 'CONCLUIDO',
  };
}
