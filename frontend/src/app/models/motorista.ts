// src/app/models/motorista.ts
export interface Motorista {
  id: number;
  nome: string;
  matricula: string;
  ativo: boolean;

  // Caso no backend o DTO detalhado inclua mais informações,
  // você pode complementar aqui (exemplo: lista de solicitações).
  solicitacoes?: any[];
}