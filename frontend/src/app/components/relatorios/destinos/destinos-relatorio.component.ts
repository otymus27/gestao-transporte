import { Component } from '@angular/core';
import { RelatorioService } from '../../../services/relatorios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-destinos-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './destinos-relatorio.component.html',
  styleUrls: ['./destinos-relatorio.component.scss'],
})
export class DestinosRelatorioComponent {
  constructor(private relatorioService: RelatorioService) {}

  filtros = {
    nome: '',
  };

  resultados: any[] = [];
  carregando = false;

  consultar(): void {
    this.carregando = true;
    this.relatorioService.listarDestinos(this.filtros).subscribe({
      next: (data) => {
        this.resultados = data.content || data;
        this.carregando = false;
      },
      error: () => (this.carregando = false),
    });
  }

  limpar(): void {
    this.filtros = { nome: '' };
    this.resultados = [];
  }

  exportar(tipo: string): void {
    const filtroGlobal = this.filtros.nome;
    this.relatorioService
      .exportarDestinos(tipo, filtroGlobal)
      .subscribe((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relatorio_destinos.${tipo}`;
        link.click();
      });
  }
}
