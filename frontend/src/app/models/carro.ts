//import { SolicitacaoResumo } from './solicitacao-resumo';

export interface Carro {
  id: number;
  placa: string;
  marca: string;
  modelo: string;
  tipo: string;
  //solicitacoes?: SolicitacaoResumo[]; // ✅ opcional, só quando buscar detalhado
}
