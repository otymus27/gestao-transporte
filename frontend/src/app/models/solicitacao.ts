export type StatusSolicitacao =
  | 'PENDENTE'
  | 'EM_ANDAMENTO'
  | 'CONCLUIDO'
  | 'CANCELADO';

export interface SolicitacaoBase {
  status: StatusSolicitacao;

  // carro e usuario REMOVIDOS — estão na FichaSolicitacao
  motoristaId?: number | null;
  setorId?: number | null;
  destinoId?: number | null;

  kmInicial?: number | null;
  kmFinal?: number | null;
  horaSaida?: string | null;
  horaChegada?: string | null;
}

export interface SolicitacaoRequest extends SolicitacaoBase {}

export interface SolicitacaoResponse extends SolicitacaoBase {
  id: number;
  dataSolicitacao?: string;

  // Campos de exibição vindos do backend
  motoristaNome?: string;
  setorNome?: string;
  destinoNome?: string;
}

export type Solicitacao = SolicitacaoResponse;

export function toSolicitacaoRequest(s: Solicitacao): SolicitacaoRequest {
  return {
    status: s.status,
    motoristaId: s.motoristaId ?? null,
    setorId: s.setorId ?? null,
    destinoId: s.destinoId ?? null,
    kmInicial: s.kmInicial ?? null,
    kmFinal: s.kmFinal ?? null,
    horaSaida: s.horaSaida ?? null,
    horaChegada: s.horaChegada ?? null,
  };
}

export function novaSolicitacao(): Solicitacao {
  return {
    id: 0,
    status: 'PENDENTE',
    motoristaId: null,
    setorId: null,
    destinoId: null,
    kmInicial: null,
    kmFinal: null,
    horaSaida: null,
    horaChegada: null,
  };
}
