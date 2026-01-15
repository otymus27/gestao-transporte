import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import {
  RelatorioSolicitacaoService,
  FiltroSolicitacaoRelatorio,
  TipoRelatorioSolicitacao,
} from '../../../services/relatorios/relatorio-solicitacao.service';

type ExportTipo = 'pdf' | 'excel' | 'csv';

@Component({
  selector: 'app-solicitacao-relatorio',
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitacao-relatorio.component.html',
  styleUrl: './solicitacao-relatorio.component.scss',
})
export class SolicitacaoRelatorioComponent {
  constructor(
    private solicitacaoService: SolicitacaoService,
    private relatorioService: RelatorioSolicitacaoService
  ) {}

  // ✅ opções pro select (pode mover pro html direto se preferir)
  tiposRelatorio: { value: TipoRelatorioSolicitacao; label: string }[] = [
    { value: 'SIMPLES', label: 'Sem agrupamento' },
    { value: 'POR_SETOR', label: 'Agrupado por Setor' },
    { value: 'POR_MOTORISTA', label: 'Agrupado por Motorista' },
    { value: 'POR_CARRO', label: 'Agrupado por Carro' },
    { value: 'POR_DESTINO', label: 'Agrupado por Destino' },
  ];

  filtros: {
    tipo: TipoRelatorioSolicitacao;
    filtro: string;
    dataInicio: string; // yyyy-MM-dd
    dataFim: string;    // yyyy-MM-dd
  } = {
    tipo: 'SIMPLES', // ✅ default (você pode trocar para POR_SETOR se quiser)
    filtro: '',
    dataInicio: '',
    dataFim: '',
  };

  resultados: any[] = [];
  carregando = false;

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  consultar(page: number = 0): void {
    this.carregando = true;
    this.page = page;

    const filtrosApi: any = {
      filtro: this.filtros.filtro?.trim() || null,
      dataInicio: this.filtros.dataInicio || null,
      dataFim: this.filtros.dataFim || null,
    };

    this.solicitacaoService
      .consultarParaRelatorio(filtrosApi, this.page, this.size)
      .subscribe({
        next: (resp) => {
          this.resultados = resp.content ?? [];
          this.totalPages = resp.totalPages ?? 0;
          this.totalElements = resp.totalElements ?? 0;
          this.page = resp.number ?? 0;
          this.size = resp.size ?? this.size;
        },
        error: (err) => {
          console.error('Erro ao consultar paginado', err);
          alert('Erro ao consultar dados.');
          this.resultados = [];
          this.totalPages = 0;
          this.totalElements = 0;
        },
        complete: () => (this.carregando = false),
      });
  }

  limpar(): void {
    this.filtros = {
      tipo: 'SIMPLES',
      filtro: '',
      dataInicio: '',
      dataFim: '',
    };

    this.resultados = [];
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
  }

  exportar(tipo: ExportTipo): void {
    // ✅ CSV depende dos dados da tabela (front)
    if (tipo === 'csv') {
      if (!this.resultados || this.resultados.length === 0) return;
      this.exportarCsv();
      return;
    }

    // ✅ PDF/Excel NÃO precisam depender da tabela.
    // Você pode exportar mesmo sem consultar antes.
    const filtrosExport: FiltroSolicitacaoRelatorio = {
      tipo: this.filtros.tipo, // ✅ aqui entra o agrupamento
      filtro: this.filtros.filtro?.trim() || null,
      dataInicio: this.filtros.dataInicio || null,
      dataFim: this.filtros.dataFim || null,
    };

    if (tipo === 'pdf') {
      this.relatorioService.exportarPdf(filtrosExport).subscribe({
        next: (blob) =>
          this.baixarArquivo(blob, this.nomeArquivo('pdf'), 'application/pdf'),
        error: (err) => {
          console.error('Erro ao exportar PDF', err);
          alert('Erro ao exportar PDF.');
        },
      });
      return;
    }

    if (tipo === 'excel') {
      this.relatorioService.exportarExcel(filtrosExport).subscribe({
        next: (blob) =>
          this.baixarArquivo(
            blob,
            this.nomeArquivo('xlsx'),
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          ),
        error: (err) => {
          console.error('Erro ao exportar Excel', err);
          alert('Erro ao exportar Excel.');
        },
      });
      return;
    }
  }

  private nomeArquivo(ext: 'pdf' | 'xlsx'): string {
    const hoje = new Date().toISOString().slice(0, 10);
    const tipo = (this.filtros.tipo || 'SEM_AGRUPAMENTO').toLowerCase();
    return `rel_solicitacoes_${tipo}_${hoje}.${ext}`;
  }

  private exportarCsv(): void {
    const header = [
      'ID',
      'Data Solicitação',
      'Status',
      'Carro',
      'Motorista',
      'Usuário',
      'Setor',
      'Destino',
      'Km Inicial',
      'Km Final',
      'Km Total',
      'Hora Saída',
      'Hora Chegada',
    ];

    const rows = this.resultados.map((r) => [
      r.id,
      r.dataSolicitacao,
      r.status,
      r.carro,
      r.motorista,
      r.usuario,
      r.setor,
      r.destino,
      r.kmInicial,
      r.kmFinal,
      r.kmTotal,
      r.horaSaida,
      r.horaChegada,
    ]);

    const csv = [header, ...rows]
      .map((cols) =>
        cols.map((v) => `"${String(v ?? '').replaceAll('"', '""')}"`).join(';')
      )
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    this.baixarArquivo(blob, 'rel_solicitacoes.csv', 'text/csv');
  }

  private baixarArquivo(blob: Blob, nome: string, contentType: string): void {
    const blobObj = new Blob([blob], { type: contentType });
    const url = window.URL.createObjectURL(blobObj);

    const a = document.createElement('a');
    a.href = url;
    a.download = nome;
    a.click();

    window.URL.revokeObjectURL(url);
  }

  mudarTamanho(event: Event): void {
    const val = Number((event.target as HTMLSelectElement).value);
    this.size = val;
    this.consultar(0);
  }

  irPara(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.consultar(p);
  }
}
