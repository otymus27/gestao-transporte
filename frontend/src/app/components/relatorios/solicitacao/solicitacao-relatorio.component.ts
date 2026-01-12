import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SolicitacaoService } from '../../../services/solicitacao.service';
import { RelatorioSolicitacaoService } from '../../../services/relatorios/relatorio-solicitacao.service';

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

  filtros = {
    filtro: '',
    dataInicio: '', // yyyy-MM-dd
    dataFim: '', // yyyy-MM-dd
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
      filtro: this.filtros.filtro,
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
    this.filtros = { filtro: '', dataInicio: '', dataFim: '' };
    this.resultados = [];
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
  }

  exportar(tipo: ExportTipo): void {
    if (!this.resultados || this.resultados.length === 0) return;

    const filtrosExport: any = {
      filtro: this.filtros.filtro,
      dataInicio: this.filtros.dataInicio || null,
      dataFim: this.filtros.dataFim || null,
    };

    if (tipo === 'pdf') {
      this.relatorioService.exportarPdf(filtrosExport).subscribe({
        next: (blob) =>
          this.baixarArquivo(blob, 'rel_solicitacoes.pdf', 'application/pdf'),
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
            'rel_solicitacoes.xlsx',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
          ),
        error: (err) => {
          console.error('Erro ao exportar Excel', err);
          alert('Erro ao exportar Excel.');
        },
      });
      return;
    }

    // CSV (gerado no front a partir dos resultados da consulta)
    this.exportarCsv();
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
      r.kmTotal, // getter no DTO (Jasper) - aqui depende se o backend retorna
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
