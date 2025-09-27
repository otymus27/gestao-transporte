import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import {
  ArquivoPublicoDTO,
  PastaPublicaDTO,
  PublicService,
} from '../../../../services/public.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-farmacia-explorer',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './farmacia.component.html',
  styleUrls: ['./farmacia.component.scss'],
})
export class FarmaciaExplorerPublicoComponent implements OnInit {
  pasta: PastaPublicaDTO | null = null;
  arquivos: ArquivoPublicoDTO[] = [];
  loading = false;

  arquivoSelecionado: ArquivoPublicoDTO | null = null;

  constructor(private publicService: PublicService, private router: Router) {}

  ngOnInit(): void {
    this.carregarArquivosFarmacia();
  }

  carregarArquivosFarmacia(): void {
    this.loading = true;
    this.publicService.getPastaFarmacia().subscribe({
      next: (detalhada) => {
        this.pasta = detalhada;
        this.arquivos = detalhada.arquivos ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar pasta Farmácia:', err);
        this.loading = false;
      },
    });
  }

  downloadArquivo(arquivo: ArquivoPublicoDTO): void {
    this.publicService.downloadArquivo(arquivo.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = arquivo.nome;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }

  visualizarArquivo(arquivo: ArquivoPublicoDTO): void {
    this.publicService.visualizarArquivo(arquivo.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
    });

    // Fecha o modal após abrir o arquivo
    this.fecharModal();
  }

  formatarTamanho(bytes: number): string {
    return this.publicService.formatarTamanho(bytes);
  }

  navegarParaHome(): void {
    this.router.navigate(['/home']);
  }

  abrirModalArquivo(arquivo: ArquivoPublicoDTO): void {
    this.arquivoSelecionado = arquivo;
  }

  fecharModal(): void {
    this.arquivoSelecionado = null;
  }

  getIcon(arquivo: ArquivoPublicoDTO): string {
    if (arquivo.nome.endsWith('.pdf')) return 'assets/icons/pdf.png';
    if (arquivo.nome.match(/\.(doc|docx)$/)) return 'assets/icons/word.png';
    if (arquivo.nome.match(/\.(xls|xlsx)$/)) return 'assets/icons/excel.png';
    return 'assets/icons/file.png';
  }
}
