export type TipoRelatorioSolicitacao =
  | 'SIMPLES'
  | 'POR_SETOR'
  | 'POR_MOTORISTA'
  | 'POR_CARRO'
  | 'POR_DESTINO';

export const TIPOS_RELATORIO_SOLICITACAO: {
  value: TipoRelatorioSolicitacao;
  label: string;
}[] = [
  { value: 'SIMPLES', label: 'Simples sem agrupamento' },
  { value: 'POR_SETOR', label: 'Agrupado por Setor' },
  { value: 'POR_MOTORISTA', label: 'Agrupado por Motorista' },
  { value: 'POR_CARRO', label: 'Agrupado por Carro' },
  { value: 'POR_DESTINO', label: 'Agrupado por Destino' },
];
