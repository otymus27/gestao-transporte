// src/app/features/admin/pages/admin-formulario/admin-formulario.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ToastService } from '../../../../services/toast.service';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  FormularioDTO,
  FormulariosService,
  PastaExplorerDTO,
} from '../../../public/services/formularios.service';
import { ErrorMessage } from '../../services/admin.service';

@Component({
  selector: 'app-admin-formulario',
  standalone: true,
  imports: [CommonModule, NgIf, FormsModule],
  templateUrl: './admin-formulario.component.html',
  styleUrls: ['./admin-formulario.component.scss'],
})
export class AdminFormularioComponent implements OnInit {
  // ---------------- Propriedades ----------------
  pastas: PastaExplorerDTO[] = [];
  arquivos: FormularioDTO[] = [];
  breadcrumb: PastaExplorerDTO[] = [];
  loading = false;

  // Modais
  modalCriarPastaAberto = false;
  modalRenomearAberto = false;
  modalUploadAberto = false;
  modalExcluirAberto = false;
  modalExcluirSelecionadosAberto = false;

  // CRUD
  novoNomePasta = '';
  itemParaRenomear: PastaExplorerDTO | FormularioDTO | null = null;
  novoNomeItem = '';
  itemParaExcluir: PastaExplorerDTO | FormularioDTO | null = null;

  // Seleção
  itensSelecionados: (PastaExplorerDTO | FormularioDTO)[] = [];
  arquivosParaUpload: File[] = [];

  // Arquivo selecionado
  arquivoSelecionado: FormularioDTO | null = null;
  modalOpcoesArquivoAberto = false;

  // Estados para ordenação
  sortBy: string = 'nomePasta';
  order: 'asc' | 'desc' = 'asc';

  constructor(
    public formulariosService: FormulariosService, // 🔑 precisa ser público pro HTML usar formatarTamanho
    public toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.carregarExplorer();
  }

  // ----------------Método para ordenação ----------------
  // 🔹 Ordenação integrada com backend
  ordenarPor(campo: 'nomePasta' | 'nomeArquivo' | 'tamanho' | 'dataUpload'): void {
    // Toggle da ordem
    if (this.sortBy === campo) {
      this.order = this.order === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = campo;
      this.order = 'asc';
    }
  
    const pastaAtual = this.obterPastaAtual();
  
    // Se estamos na RAIZ:
    if (!pastaAtual) {
      this.loading = true;
      this.formulariosService.listarExplorer(this.sortBy, this.order).subscribe({
        next: (pastasOrdenadas) => {
          this.pastas = pastasOrdenadas; // só pastas
          this.arquivos = [];            // raiz não tem arquivos diretos
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao ordenar as pastas', err),
      });
      return;
    }
  
    // Se estamos DENTRO de uma pasta:
    this.loading = true;
    this.formulariosService.listarExplorer(this.sortBy, this.order).subscribe({
      next: (arvoreOrdenada) => {
        // Encontrar a pasta atual na árvore ordenada
        const encontrada = this.findPastaById(arvoreOrdenada, pastaAtual.id);
        this.pastas = encontrada?.subPastas ?? [];
        this.arquivos = encontrada?.formularios ?? [];
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao ordenar conteúdo da pasta', err),
    });
  }

  // 🔹 Carrega conteúdo (pastas + arquivos) já ordenado
  carregarExplorer(): void {
    this.loading = true;
    this.formulariosService.listarExplorer(this.sortBy, this.order).subscribe({
      next: (pastas) => {
        this.pastas = pastas;
        this.arquivos = [];
        this.breadcrumb = [];
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao carregar explorer', err),
    });
  }

  // ---------------- Opções de Arquivo ----------------
  abrirOpcoesArquivo(arquivo: FormularioDTO): void {
    this.arquivoSelecionado = arquivo;
    this.modalOpcoesArquivoAberto = true;
  }

  fecharModalOpcoesArquivo(): void {
    this.arquivoSelecionado = null;
    this.modalOpcoesArquivoAberto = false;
  }

  visualizarArquivo(arquivo: FormularioDTO): void {
    this.formulariosService.downloadFormulario(arquivo.id, 'inline').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank'); // abre em nova aba para visualização
      },
      error: (err) => this.handleError('Erro ao visualizar arquivo', err),
    });

    this.fecharModalOpcoesArquivo();
  }

  abrirPasta(pasta: PastaExplorerDTO, adicionarBreadcrumb = true): void {
    if (!pasta) return;
    if (adicionarBreadcrumb) this.breadcrumb.push(pasta);

    this.loading = true;

    // ✅ Agora não chama mais o backend
    // apenas usa o que já está no objeto `pasta`
    this.pastas = pasta.subPastas ?? [];
    this.arquivos = pasta.formularios ?? [];

    this.loading = false;
  }

  navegarPara(index: number): void {
    if (index < 0) return this.carregarExplorer();
    this.breadcrumb = this.breadcrumb.slice(0, index + 1);
    this.abrirPasta(this.breadcrumb[this.breadcrumb.length - 1], false);
  }

  voltarUmNivel(): void {
    this.navegarPara(this.breadcrumb.length - 2);
  }

  public obterPastaAtual(): PastaExplorerDTO | undefined {
    return this.breadcrumb[this.breadcrumb.length - 1];
  }

  // ---------------- CRUD ----------------
  abrirModalCriarPasta(): void {
    this.modalCriarPastaAberto = true;
    this.novoNomePasta = '';
  }

  fecharModalCriarPasta(): void {
    this.modalCriarPastaAberto = false;
  }

  criarPasta(): void {
    if (!this.novoNomePasta) return;
    this.formulariosService.criarPasta(this.novoNomePasta).subscribe({
      next: () => {
        this.toastService.showSuccess('Pasta criada com sucesso!');
        this.carregarExplorer();
        this.fecharModalCriarPasta();
      },
      error: (err) => this.handleError('Erro ao criar pasta', err),
    });
  }

  abrirModalRenomear(item: PastaExplorerDTO | FormularioDTO): void {
    this.itemParaRenomear = item;
    // defensivo: usa a propriedade correta, com fallback pra string vazia
    this.novoNomeItem = this.isPasta(item)
      ? item.nomePasta ?? ''
      : item.nomeArquivo ?? '';
    this.modalRenomearAberto = true;
  }

  fecharModalRenomear(): void {
    this.modalRenomearAberto = false;
    this.itemParaRenomear = null;
    this.novoNomeItem = '';
  }

  // Chama função no service para renomear arquivo ou pasta
renomearItem(): void {
  if (!this.itemParaRenomear || !this.novoNomeItem) return;

  const request: Observable<any> = this.isPasta(this.itemParaRenomear)
    ? this.formulariosService.renomearPasta(
        this.itemParaRenomear.id,
        this.novoNomeItem
      )
    : this.formulariosService.renomearFormulario(
        this.itemParaRenomear.id,
        this.novoNomeItem // 🔑 passa só o novo nome, não cria `File`
      );

  request.subscribe({
    next: () => {
      this.toastService.showSuccess('Nome alterado com sucesso!');
      this.fecharModalRenomear();
      this.recarregarAtual();
    },
    error: (err) => {
      this.handleError('Erro ao renomear item', err);
      this.fecharModalRenomear();
    },
  });
}


  // ---------------- Upload ----------------
  abrirModalUpload(): void {
    this.modalUploadAberto = true;
  }

  fecharModalUpload(): void {
    this.modalUploadAberto = false;
    this.arquivosParaUpload = [];
  }

  onArquivosSelecionados(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input?.files) {
      const novosArquivos = Array.from(input.files);
      this.arquivosParaUpload.push(...novosArquivos);
    }
  }

  removerArquivoSelecionado(file: File): void {
    this.arquivosParaUpload = this.arquivosParaUpload.filter((f) => f !== file);
  }

  uploadArquivos(): void {
    const pastaAtual = this.obterPastaAtual();
    if (!pastaAtual) return this.handleError('Selecione uma pasta');
    if (!this.arquivosParaUpload.length) return;

    this.loading = true;
    this.formulariosService
      .uploadMultiplos(pastaAtual.id, this.arquivosParaUpload)
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Upload realizado com sucesso!');
          this.fecharModalUpload();
          this.recarregarAtual(); // 🔑 recarrega sem voltar pra raiz
          this.loading = false;
        },
        error: (err) =>
          this.handleError('Erro ao fazer upload dos arquivos', err),
      });
  }

  // ---------------- Exclusão ----------------
  abrirModalExcluir(item: PastaExplorerDTO | FormularioDTO): void {
    this.itemParaExcluir = item;
    this.modalExcluirAberto = true;
  }

  fecharModalExcluir(): void {
    this.modalExcluirAberto = false;
    this.itemParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.itemParaExcluir) return;

    const request: Observable<void> = this.isPasta(this.itemParaExcluir)
      ? this.formulariosService.excluirPasta(this.itemParaExcluir.id)
      : this.formulariosService.excluirFormulario(this.itemParaExcluir.id);

    this.loading = true;
    request.subscribe({
      next: () => {
        this.toastService.showSuccess('Exclusão feita com sucesso!');
        this.fecharModalExcluir();
        this.recarregarAtual(); // 🔑 sempre atualiza
        this.loading = false;
      },
      error: (err: any) => {
        this.handleError('Erro ao excluir item', err);
        this.loading = false;
      },
    });
  }

  // --- novos métodos para excluir em lote ---
  abrirModalExcluirSelecionados(): void {
    if (this.itensSelecionados.length > 0) {
      this.modalExcluirSelecionadosAberto = true;
    }
  }

  fecharModalExcluirSelecionados(): void {
    this.modalExcluirSelecionadosAberto = false;
  }

  confirmarExclusaoSelecionados(): void {
    if (!this.itensSelecionados.length) return;

    const pastasSelecionadas = this.itensSelecionados.filter((i) =>
      this.isPasta(i)
    ) as PastaExplorerDTO[];

    const arquivosSelecionados = this.itensSelecionados.filter(
      (i) => !this.isPasta(i)
    ) as FormularioDTO[];

    const requests: Observable<any>[] = [];

    if (arquivosSelecionados.length) {
      const ids = arquivosSelecionados.map((a) => a.id);
      requests.push(
        this.formulariosService.excluirVarios(ids).pipe(
          catchError((err) => {
            this.handleError('Erro ao excluir formulários em lote', err);
            return of(null);
          })
        )
      );
    }

    pastasSelecionadas.forEach((p) => {
      requests.push(
        this.formulariosService.excluirPasta(p.id).pipe(
          catchError((err) => {
            this.handleError(`Erro ao excluir pasta "${p.nomePasta}"`, err);
            return of(null);
          })
        )
      );
    });

    this.loading = true;

    forkJoin(requests).subscribe({
      next: () => {
        this.toastService.showSuccess('Itens excluídos com sucesso!');
        this.itensSelecionados = [];
        this.modalExcluirSelecionadosAberto = false;
        this.recarregarAtual(); // 🔑 força atualizar do backend
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Erro ao excluir itens selecionados', err);
        this.loading = false;
      },
    });
  }

  // ---------------- Download ----------------
  downloadArquivo(arquivo: FormularioDTO): void {
    this.formulariosService
      .downloadFormulario(arquivo.id, 'download')
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = arquivo.nomeArquivo;
          a.click();
          URL.revokeObjectURL(url);
        },
        error: (err) => this.handleError('Erro ao baixar arquivo', err),
      });
  }

  // ---------------- Seleção ----------------
  isSelecionado(item: PastaExplorerDTO | FormularioDTO): boolean {
    return this.itensSelecionados.includes(item);
  }

  toggleSelecao(item: PastaExplorerDTO | FormularioDTO): void {
    this.isSelecionado(item)
      ? (this.itensSelecionados = this.itensSelecionados.filter(
          (i) => i !== item
        ))
      : this.itensSelecionados.push(item);
  }

  get todosItens(): (PastaExplorerDTO | FormularioDTO)[] {
    return [...this.pastas, ...this.arquivos];
  }

  marcarTodos(): void {
    this.itensSelecionados = this.todosItens;
  }

  desmarcarTodos(): void {
    this.itensSelecionados = [];
  }

  inverterSelecao(): void {
    this.itensSelecionados = this.todosItens.filter(
      (item) => !this.isSelecionado(item)
    );
  }

  // ---------------- Helpers ----------------
  isPasta(item: PastaExplorerDTO | FormularioDTO): item is PastaExplorerDTO {
    return (item as PastaExplorerDTO).nomePasta !== undefined;
  }

  getNomeItem(item: PastaExplorerDTO | FormularioDTO | null): string {
    return item ? (this.isPasta(item) ? item.nomePasta : item.nomeArquivo) : '';
  }

  private handleError(mensagemPadrao: string, err?: any): void {
    console.error(mensagemPadrao, err);

    let errorMessage = mensagemPadrao;
    if (err && typeof err === 'object' && 'status' in err) {
      const backend = err as ErrorMessage;
      errorMessage = `Erro (${backend.status}${
        backend.error ? ' - ' + backend.error : ''
      }): ${backend.message || mensagemPadrao}`;
    }

    this.toastService.showError(errorMessage);
    this.loading = false;
  }

 // ♻️ Recarrega mantendo onde você está (após renomear, upload, excluir, etc)
private recarregarAtual(): void {
  const atual = this.obterPastaAtual();
  this.loading = true;

  // Se estiver na raiz, só recarrega a raiz com sort atual
  if (!atual) {
    this.formulariosService.listarExplorer(this.sortBy, this.order).subscribe({
      next: (pastas) => {
        this.pastas = pastas;
        this.arquivos = [];
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao atualizar raiz', err),
    });
    return;
  }

  // Dentro de uma pasta → busca a árvore ordenada e extrai a pasta atual
  this.formulariosService.listarExplorer(this.sortBy, this.order).subscribe({
    next: (pastas) => {
      const encontrada = this.findPastaById(pastas, atual.id);
      this.pastas = encontrada?.subPastas ?? [];
      this.arquivos = encontrada?.formularios ?? [];
      this.loading = false;
    },
    error: (err) => this.handleError('Erro ao atualizar pasta', err),
  });
}

// 🔎 Busca recursiva (você já tem algo assim; mantenha)
private findPastaById(pastas: PastaExplorerDTO[], id: number): PastaExplorerDTO | null {
  for (const p of pastas) {
    if (p.id === id) return p;
    if (p.subPastas?.length) {
      const encontrada = this.findPastaById(p.subPastas, id);
      if (encontrada) return encontrada;
    }
  }
  return null;
}
}
