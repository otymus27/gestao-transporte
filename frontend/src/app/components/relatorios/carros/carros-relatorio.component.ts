import { Component } from '@angular/core';
import { RelatorioService } from '../../../services/relatorios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-relatorio',
  imports: [CommonModule, FormsModule], // ✅ inclui módulos necessários
  templateUrl: './carros-relatorio.component.html',
  styleUrl: './carros-relatorio.component.scss',
})
export class CarrosRelatorioComponent {
  constructor(private relatorioService: RelatorioService) {}

  filtros = {
    placa: '',
    marca: '',
    modelo: '',
  };

  resultados: any[] = [];
  carregando = false;

  consultar(): void {
    this.carregando = true;
    this.relatorioService.listarCarros(this.filtros).subscribe({
      next: (data) => {
        this.resultados = data.content || data;
        this.carregando = false;
      },
      error: () => (this.carregando = false),
    });
  }

  limpar(): void {
    this.filtros = { placa: '', marca: '', modelo: '' };
    this.resultados = [];
  }

  exportar(tipo: string): void {
    const filtroGlobal =
      this.filtros.placa || this.filtros.marca || this.filtros.modelo;
    this.relatorioService
      .exportarCarros(tipo, filtroGlobal)
      .subscribe((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relatorio_carros.${tipo}`;
        link.click();
      });
  }
}
