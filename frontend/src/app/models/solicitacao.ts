/**
 * Estrutura base exigida pelo backend para Solicitação
 */
export interface SolicitacaoBase {
  dataSolicitacao: string;   // formato ISO ou yyyy-MM-ddTHH:mm
  status: string;

  carroId?: number | null;
  motoristaId?: number | null;
  usuarioId?: number | null;
  setorId?: number | null;
  destinoId?: number | null;

  kmInicial?: number | null;
  kmFinal?: number | null;
  horaSaida?: string | null;    // HH:mm
  horaChegada?: string | null;  // HH:mm
}

/**
 * Request enviado ao backend
 */
export interface SolicitacaoRequest extends SolicitacaoBase {}

/**
 * Response vindo do backend (lista, detalhado etc.)
 */
export interface SolicitacaoResponse extends SolicitacaoBase {
  id: number;

  // Campos adicionais para exibição
  carroPlaca?: string;
  motoristaNome?: string;
  usuarioNome?: string;
  setorNome?: string;
  destinoNome?: string;
}

/**
 * Tipo principal usado no frontend
 */
export type Solicitacao = SolicitacaoResponse;

/**
 * Função helper: monta payload para envio ao backend
 */
export function toSolicitacaoRequest(s: Solicitacao): SolicitacaoRequest {
  return {
    dataSolicitacao: s.dataSolicitacao,
    status: s.status,

    carroId: s.carroId ?? null,
    motoristaId: s.motoristaId ?? null,
    usuarioId: s.usuarioId ?? null,
    setorId: s.setorId ?? null,
    destinoId: s.destinoId ?? null,

    kmInicial: s.kmInicial ?? null,
    kmFinal: s.kmFinal ?? null,
    horaSaida: s.horaSaida ?? null,
    horaChegada: s.horaChegada ?? null,
  };
}

/**
 * Factory para iniciar uma nova solicitação no formulário
 */
export function novaSolicitacao(): Solicitacao {
  return {
    id: 0,
    status: '',
    dataSolicitacao: new Date().toISOString().slice(0, 16), // para input datetime-local

    carroId: null,
    motoristaId: null,
    usuarioId: null,
    setorId: null,
    destinoId: null,

    kmInicial: null,
    kmFinal: null,
    horaSaida: null,
    horaChegada: null,
  };
}
