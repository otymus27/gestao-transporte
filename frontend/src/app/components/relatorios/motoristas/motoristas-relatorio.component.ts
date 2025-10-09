import { Component } from '@angular/core';
import { RelatorioService } from '../../../services/relatorios.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-motoristas-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './motoristas-relatorio.component.html',
  styleUrls: ['./motoristas-relatorio.component.scss'],
})
export class MotoristasRelatorioComponent {
  constructor(private relatorioService: RelatorioService) {}

  filtros = {
    nome: '',
    matricula: '',
  };

  resultados: any[] = [];
  carregando = false;

  consultar(): void {
    this.carregando = true;
    this.relatorioService.listarMotoristas(this.filtros).subscribe({
      next: (data) => {
        this.resultados = data.content || data;
        this.carregando = false;
      },
      error: () => (this.carregando = false),
    });
  }

  limpar(): void {
    this.filtros = { nome: '', matricula: '' };
    this.resultados = [];
  }

  exportar(tipo: string): void {
    this.relatorioService
      .exportarMotoristas(tipo, this.filtros)
      .subscribe((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relatorio_motoristas.${tipo}`;
        link.click();
      });
  }
}
