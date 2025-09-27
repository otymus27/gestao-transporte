import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  FormularioDTO,
  FormulariosService,
  PastaExplorerDTO,
} from '../../services/formularios.service';

@Component({
  selector: 'app-explorer-form-public',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './explorer-form-public.component.html',
  styleUrls: ['./explorer-form-public.component.scss'],
})
export class ExplorerFormPublicComponent implements OnInit {
  pastas: PastaExplorerDTO[] = [];
  pastaAtual:
    | (PastaExplorerDTO & {
        subPastas: PastaExplorerDTO[];
        formularios: FormularioDTO[];
      })
    | null = null;
  breadcrumb: PastaExplorerDTO[] = [];
  arquivos: FormularioDTO[] = [];
  loading = false;

  // Modal
  arquivoSelecionado?: FormularioDTO;

  constructor(
    public formulariosService: FormulariosService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregarPastas();
  }

  carregarPastas(): void {
    this.loading = true;
    this.formulariosService.listarExplorer().subscribe({
      next: (pastas) => {
        this.pastas = pastas.map((p) => ({
          ...p,
          subPastas: p.subPastas ?? [],
          formularios: p.formularios ?? [],
        }));
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  abrirPasta(pasta: PastaExplorerDTO): void {
    this.breadcrumb.push(pasta);
    this.pastaAtual = {
      ...pasta,
      subPastas: pasta.subPastas ?? [],
      formularios: pasta.formularios ?? [],
    };
    this.arquivos = this.pastaAtual.formularios;
  }

  abrirSubPasta(subPasta: PastaExplorerDTO): void {
    this.abrirPasta(subPasta);
  }

  navegarPara(index: number): void {
    if (index < 0) {
      this.breadcrumb = [];
      this.pastaAtual = null;
      this.arquivos = [];
      return;
    }
    this.breadcrumb = this.breadcrumb.slice(0, index + 1);
    const pasta = this.breadcrumb[this.breadcrumb.length - 1];
    this.pastaAtual = {
      ...pasta,
      subPastas: pasta.subPastas ?? [],
      formularios: pasta.formularios ?? [],
    };
    this.arquivos = this.pastaAtual.formularios;
  }

  navegarParaHome(): void {
    this.router.navigate(['/home']);
  }

  abrirArquivo(arquivo: FormularioDTO) {
    this.arquivoSelecionado = arquivo;
  }

  fecharModal() {
    this.arquivoSelecionado = undefined;
  }

  baixarArquivo(arquivo: FormularioDTO) {
    this.formulariosService
      .downloadFormulario(arquivo.id, 'download')
      .subscribe((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = arquivo.nomeArquivo;
        a.click();
        URL.revokeObjectURL(url);
        this.fecharModal();
      });
  }

  visualizarArquivo(arquivo: FormularioDTO): void {
    this.formulariosService
      .downloadFormulario(arquivo.id, 'inline')
      .subscribe((blob) => {
        let tipoMimeCorrigido = 'application/pdf';
        if (arquivo.nomeArquivo.toLowerCase().endsWith('.pdf')) {
          tipoMimeCorrigido = 'application/pdf';
        }
        const blobComTipo = new Blob([blob], { type: tipoMimeCorrigido });
        const url = window.URL.createObjectURL(blobComTipo);
        window.open(url, '_blank');
        this.fecharModal();
      });
  }

  getIcon(arquivo: FormularioDTO): string {
    const extensao = arquivo.nomeArquivo.split('.').pop()?.toLowerCase();

    switch (extensao) {
      case 'pdf':
        return 'assets/icons/pdf.png';
      case 'doc':
      case 'docx':
        return 'assets/icons/word.png';
      case 'xls':
      case 'xlsx':
        return 'assets/icons/xls.png';
      case 'ppt':
      case 'pptx':
        return 'assets/icons/ppt.png';
      case 'txt':
        return 'assets/icons/txt.png';
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
        return 'assets/icons/image.png';
      default:
        return 'assets/icons/file.png';
    }
  }

  voltar() {
    this.navegarPara(this.breadcrumb.length - 2);
  }
}
