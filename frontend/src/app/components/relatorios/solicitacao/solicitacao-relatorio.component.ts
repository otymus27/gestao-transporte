import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { DashboardService } from '../../../services/dashboard.service';
import {
  RelatorioSolicitacaoService,
  FiltroSolicitacaoRelatorio,
  TipoRelatorioSolicitacao,
} from '../../../services/relatorios/relatorio-solicitacao.service';

type ExportTipo = 'pdf' | 'excel' | 'csv';

@Component({
  selector: 'app-solicitacao-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitacao-relatorio.component.html',
  styleUrl: './solicitacao-relatorio.component.scss',
})
export class SolicitacaoRelatorioComponent implements OnInit {
  constructor(
    private solicitacaoService: SolicitacaoService,
    private relatorioService: RelatorioSolicitacaoService,
    private dashboardService: DashboardService
  ) {}

  // Cards de resumo
  totalSolicitacoes = 0;
  totalConcluidas = 0;
  totalPendentes = 0;
  totalCanceladas = 0;

  tiposRelatorio: { value: TipoRelatorioSolicitacao; label: string }[] = [
    { value: 'SIMPLES', label: 'Sem agrupamento' },
    { value: 'POR_SETOR', label: 'Agrupado por Setor' },
    { value: 'POR_MOTORISTA', label: 'Agrupado por Motorista' },
    { value: 'POR_CARRO', label: 'Agrupado por Veículo' },
    { value: 'POR_DESTINO', label: 'Agrupado por Destino' },
    { value: 'POR_USUARIO', label: 'Agrupado por Usuário' },
  ];

  filtros = {
    tipo: 'SIMPLES' as TipoRelatorioSolicitacao,
    filtro: '',
    dataInicio: '',
    dataFim: '',
  };

  resultados: any[] = [];
  carregando = false;
  consultado = false;

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  ngOnInit(): void {
    this.carregarResumo();
  }

  carregarResumo(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (d) => {
        this.totalSolicitacoes = d.totalSolicitacoes;
        this.totalConcluidas = d.solicitacoesFinalizadas;
        this.totalPendentes = d.solicitacoesEmAndamento;
        this.totalCanceladas = d.solicitacoesCanceladas;
      },
    });
  }

  consultar(page = 0): void {
    this.carregando = true;
    this.consultado = true;
    this.page = page;

    this.solicitacaoService
      .consultarParaRelatorio(
        {
          filtro: this.filtros.filtro?.trim() || null,
          dataInicio: this.filtros.dataInicio || null,
          dataFim: this.filtros.dataFim || null,
        },
        this.page,
        this.size
      )
      .subscribe({
        next: (resp) => {
          this.resultados = resp.content ?? [];
          this.totalPages = resp.totalPages ?? 0;
          this.totalElements = resp.totalElements ?? 0;
          this.page = resp.number ?? 0;
          this.size = resp.size ?? this.size;
        },
        error: () => {
          this.resultados = [];
          this.totalPages = 0;
        },
        complete: () => (this.carregando = false),
      });
  }

  limpar(): void {
    this.filtros = { tipo: 'SIMPLES', filtro: '', dataInicio: '', dataFim: '' };
    this.resultados = [];
    this.page = 0;
    this.totalPages = 0;
    this.consultado = false;
  }

  exportar(tipo: ExportTipo): void {
    if (tipo === 'csv') {
      if (!this.resultados.length) return;
      this.exportarCsv();
      return;
    }

    const filtrosExport: FiltroSolicitacaoRelatorio = {
      tipo: this.filtros.tipo,
      filtro: this.filtros.filtro?.trim() || null,
      dataInicio: this.filtros.dataInicio || null,
      dataFim: this.filtros.dataFim || null,
    };

    const obs$ =
      tipo === 'pdf'
        ? this.relatorioService.exportarPdf(filtrosExport)
        : this.relatorioService.exportarExcel(filtrosExport);

    const ext = tipo === 'pdf' ? 'pdf' : 'xlsx';
    const contentType =
      tipo === 'pdf'
        ? 'application/pdf'
        : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

    obs$.subscribe({
      next: (blob) =>
        this.baixarArquivo(blob, this.nomeArquivo(ext as any), contentType),
      error: (err) => console.error('Erro ao exportar', err),
    });
  }

  mudarTamanho(event: Event): void {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.consultar(0);
  }

  irPara(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.consultar(p);
  }

  get percentualConcluidas(): string {
    if (!this.totalSolicitacoes) return '0';
    return ((this.totalConcluidas / this.totalSolicitacoes) * 100).toFixed(0);
  }

  private nomeArquivo(ext: 'pdf' | 'xlsx'): string {
    const hoje = new Date().toISOString().slice(0, 10);
    return `rel_solicitacoes_${this.filtros.tipo.toLowerCase()}_${hoje}.${ext}`;
  }

  private exportarCsv(): void {
    const header = [
      'ID',
      'Data',
      'Status',
      'Veículo',
      'Motorista',
      'Usuário',
      'Setor',
      'Destino',
      'KM Ini',
      'KM Fin',
      'KM Total',
      'Saída',
      'Chegada',
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
      .map((c) =>
        c.map((v) => `"${String(v ?? '').replaceAll('"', '""')}"`).join(';')
      )
      .join('\n');
    this.baixarArquivo(
      new Blob([csv], { type: 'text/csv;charset=utf-8;' }),
      'rel_solicitacoes.csv',
      'text/csv'
    );
  }

  private baixarArquivo(blob: Blob, nome: string, contentType: string): void {
    const url = window.URL.createObjectURL(
      new Blob([blob], { type: contentType })
    );
    const a = document.createElement('a');
    a.href = url;
    a.download = nome;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
