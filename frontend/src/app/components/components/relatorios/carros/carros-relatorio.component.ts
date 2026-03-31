import { Component } from '@angular/core';
import { RelatorioService } from '../../../services/relatorios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CarroService } from '../../../services/carro.service';
import { RelatorioCarroService } from '../../../services/relatorios/relatorio-carro.service';

type ExportTipo = 'pdf' | 'excel' | 'csv';
@Component({
  selector: 'app-relatorio',
  imports: [CommonModule, FormsModule], // ✅ inclui módulos necessários
  templateUrl: './carros-relatorio.component.html',
  styleUrl: './carros-relatorio.component.scss',
})
export class CarrosRelatorioComponent {
  constructor(
    private carroService: CarroService,
    private relatorioService: RelatorioCarroService
  ) {}

  filtros = {
    placa: '',
    marca: '',
    modelo: '',
    tipo: '',
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
    this.carroService
      .consultarParaRelatorio(this.filtros, this.page, this.size)
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
    this.filtros = { placa: '', marca: '', modelo: '', tipo: '' };
    this.resultados = [];
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
  }

  exportar(tipo: ExportTipo): void {
    if (!this.resultados || this.resultados.length === 0) return;

    if (tipo === 'pdf') {
      this.relatorioService.exportarPdf(this.filtros).subscribe({
        next: (blob) =>
          this.baixarArquivo(blob, 'rel_carros.pdf', 'application/pdf'),
        error: (err) => {
          console.error('Erro ao exportar PDF', err);
          alert('Erro ao exportar PDF.');
        },
      });
      return;
    }

    if (tipo === 'excel') {
      this.relatorioService.exportarExcel(this.filtros).subscribe({
        next: (blob) =>
          this.baixarArquivo(
            blob,
            'rel_carros.xlsx',
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
    const header = ['ID', 'Placa', 'Marca', 'Modelo', 'Tipo'];
    const rows = this.resultados.map((r) => [
      r.id,
      r.placa,
      r.marca,
      r.modelo,
      r.tipo,
    ]);

    const csv = [header, ...rows]
      .map((cols) =>
        cols.map((v) => `"${String(v ?? '').replaceAll('"', '""')}"`).join(';')
      )
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    this.baixarArquivo(blob, 'rel_carros.csv', 'text/csv');
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
