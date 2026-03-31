import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MotoristaService } from '../../../services/motorista.service';
import { RelatorioMotoristaService } from '../../../services/relatorios/relatorio-motorista.service';

type ExportTipo = 'pdf' | 'excel' | 'csv';

@Component({
  selector: 'app-motoristas-relatorio',
  imports: [CommonModule, FormsModule],
  templateUrl: './motoristas-relatorio.component.html',
  styleUrl: './motoristas-relatorio.component.scss',
})
export class MotoristasRelatorioComponent {
  constructor(
    private motoristaService: MotoristaService,
    private relatorioService: RelatorioMotoristaService
  ) {}

  filtros = {
    matricula: '',
    nome: '',
    telefone: '',
    // '', 'true', 'false' para facilitar binding no select do HTML
    ativo: '' as '' | 'true' | 'false',
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

    // converte ativo string -> boolean | null
    const filtrosApi: any = {
      ...this.filtros,
      ativo:
        this.filtros.ativo === ''
          ? null
          : this.filtros.ativo === 'true'
          ? true
          : false,
    };

    this.motoristaService
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
    this.filtros = { matricula: '', nome: '', telefone: '', ativo: '' };
    this.resultados = [];
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
  }

  exportar(tipo: ExportTipo): void {
    if (!this.resultados || this.resultados.length === 0) return;

    // converte ativo string -> boolean | null
    const filtrosExport: any = {
      matricula: this.filtros.matricula,
      nome: this.filtros.nome,
      telefone: this.filtros.telefone,
      ativo:
        this.filtros.ativo === ''
          ? null
          : this.filtros.ativo === 'true'
          ? true
          : false,
    };

    if (tipo === 'pdf') {
      this.relatorioService.exportarPdf(filtrosExport).subscribe({
        next: (blob) =>
          this.baixarArquivo(blob, 'rel_motoristas.pdf', 'application/pdf'),
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
            'rel_motoristas.xlsx',
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
    const header = ['ID', 'Matrícula', 'Nome', 'Telefone', 'Ativo'];
    const rows = this.resultados.map((r) => [
      r.id,
      r.matricula,
      r.nome,
      r.telefone,
      // backend pode mandar boolean; se já vier "Sim/Não", ok também
      typeof r.ativo === 'boolean' ? (r.ativo ? 'Sim' : 'Não') : r.ativo ?? '',
    ]);

    const csv = [header, ...rows]
      .map((cols) =>
        cols.map((v) => `"${String(v ?? '').replaceAll('"', '""')}"`).join(';')
      )
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    this.baixarArquivo(blob, 'rel_motoristas.csv', 'text/csv');
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
