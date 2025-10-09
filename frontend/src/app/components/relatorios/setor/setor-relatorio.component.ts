import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RelatorioService } from '../../../services/relatorios.service';

@Component({
  selector: 'app-setores-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './setor-relatorio.component.html',
  styleUrls: ['./setor-relatorio.component.scss'],
})
export class SetoresRelatorioComponent {
  constructor(private relatorioService: RelatorioService) {}

  filtros = { nome: '' };
  resultados: any[] = [];
  carregando = false;

  consultar(): void {
    this.carregando = true;
    this.relatorioService.listarSetores(this.filtros).subscribe({
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
    this.relatorioService
      .exportarSetores(tipo, this.filtros.nome)
      .subscribe((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relatorio_setores.${tipo}`;
        link.click();
      });
  }
}
