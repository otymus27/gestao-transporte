import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AdminService,
  PastaCompletaDTO,
  ArquivoAdmin,
  PastaExcluirDTO,
  UsuarioResumoDTO,
  PastaPermissaoAcaoDTO,
  Paginacao,
} from '../../services/admin.service';
import { Usuario } from '../../../../models/usuario';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ModalUsuarioComponent } from '../modal-usuario/modal-usuario.component';
import { ToastService } from '../../../../services/toast.service';
import { ErrorMessage } from '../../../../services/usuario.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-admin-explorer',
  standalone: true,
  imports: [CommonModule, NgIf, FormsModule, ModalUsuarioComponent],
  templateUrl: './admin-explorer.component.html',
  styleUrls: ['./admin-explorer.component.scss'],
})
export class AdminExplorerComponent implements OnInit {
  private route = inject(ActivatedRoute);

  // ---------------- Conteúdo ----------------
  pastas: PastaCompletaDTO[] = [];
  arquivos: ArquivoAdmin[] = [];
  breadcrumb: PastaCompletaDTO[] = [];
  loading = false;

  // ---------------- Modais ----------------
  modalCriarPastaAberto = false;
  modalRenomearAberto = false;
  modalUploadAberto = false;
  modalExcluirAberto = false;
  modalExcluirSelecionadosAberto = false;
  modalPermissaoAberto = false;
  modalCopiarAberto = false;
  modalCopiarPastaAberto = false;
  modalMoverPastaAberto = false;
  modalMoverArquivoAberto = false;
  modalSelecionarUsuarioAberto = false;
  modalOpcoesArquivoAberto = false;

  // ------------ORDENAÇÃO------------
  ordenarPor: string = 'nome'; // campo padrão
  ordemAsc: boolean = true; // ordem padrão

  // ------------FILTROS------------
  filtroNome: string = '';

  // ---------------- CRUD ----------------
  novoNomePasta = '';
  itemParaRenomear: PastaCompletaDTO | ArquivoAdmin | null = null;
  novoNomeItem: string = '';
  itemParaExcluir: PastaCompletaDTO | ArquivoAdmin | null = null;

  // ---------------- Upload ----------------
  arquivosParaUpload: File[] = [];

  // ---------------- Seleção ----------------
  itensSelecionados: (PastaCompletaDTO | ArquivoAdmin)[] = [];

  // ---------------- Usuários ----------------
  usuarios: Usuario[] = [];
  usuariosSelecionados: Usuario[] = [];
  usuariosComPermissao: UsuarioResumoDTO[] = [];
  usuariosDisponiveis: Usuario[] = [];
  usuariosComPermissaoIds: number[] = [];
  private usuariosIniciaisComPermissao: UsuarioResumoDTO[] = [];
  usuariosExcluidosIds: number[] = [];
  pastaParaPermissao: PastaCompletaDTO | null = null;
  modalAtivo: 'criarPasta' | 'permissao' | null = null;

  // ---------------- Copiar / Mover ----------------
  arquivoParaCopiar: ArquivoAdmin | null = null;
  pastaDestinoSelecionada: PastaCompletaDTO | null = null;
  pastasDisponiveisParaCopiar: PastaCompletaDTO[] = [];

  pastaParaCopiar: PastaCompletaDTO | null = null;
  pastaDestinoMover: PastaCompletaDTO | null = null;
  pastaParaMover: PastaCompletaDTO | null = null;
  pastasDisponiveisParaMover: PastaCompletaDTO[] = [];

  arquivoParaMover: ArquivoAdmin | null = null;
  pastaDestinoArquivo: PastaCompletaDTO | null = null;

  // ---------------- Arquivo ----------------
  arquivoSelecionado: ArquivoAdmin | null = null;

  constructor(
    public adminService: AdminService, // 🔑 precisa ser público pro HTML usar formatarTamanho
    //private adminService: AdminService,
    public toastService: ToastService
  ) {}

  ngOnInit(): void {
    const idPastaInicial = this.route.snapshot.data['pastaId'];

    if (idPastaInicial) {
      // Abre direto a pasta configurada na rota (ex: Farmácia)
      this.loading = true;
      this.adminService
        .getPastaPorId(idPastaInicial, this.ordenarPor, this.ordemAsc)
        .subscribe({
          next: (pasta) => {
            this.breadcrumb = [pasta]; // já adiciona no breadcrumb
            this.pastas = pasta.subPastas ?? [];
            this.arquivos = pasta.arquivos ?? [];
            this.loading = false;
          },
          error: (err) =>
            this.handleError('Erro ao carregar pasta inicial', err),
        });
    } else {
      // Fluxo normal (raiz)
      this.recarregarConteudo();
    }

    this.carregarUsuarios();
  }

  // ---------------- Ordenação ----------------
  ordenarPorCampo(campo: string): void {
    if (this.ordenarPor === campo) {
      // se clicar no mesmo campo, inverte a ordem
      this.ordemAsc = !this.ordemAsc;
    } else {
      // senão, troca o campo e volta para ascendente
      this.ordenarPor = campo;
      this.ordemAsc = true;
    }

    // recarrega conteúdo já ordenado
    this.recarregarConteudo();
  }

  // ---------------- Busca por nome ----------------
  buscar(limparDepois: boolean = false): void {
    this.loading = true;

    this.adminService
      .listarConteudoRaiz(this.ordenarPor, this.ordemAsc, this.filtroNome)
      .subscribe({
        next: (pastas) => {
          // 🔹 Coleta todas as pastas que bateram (já vem filtradas do backend)
          this.pastas = pastas;

          // 🔹 Coleta arquivos em qualquer nível (flatten recursivo)
          this.arquivos = this.coletarArquivosRecursivo(pastas);

          console.log('Pastas recebidas:', pastas);
          console.log('Arquivos coletados:', this.arquivos);

          // 🔹 Zera o breadcrumb porque é busca global
          this.breadcrumb = [];
          this.loading = false;

          if (limparDepois) {
            this.filtroNome = '';
          }
        },
        error: (err) => this.handleError('Erro ao buscar conteúdo', err),
      });
  }
  // ---------------- Navegação ----------------
  recarregarConteudo(): void {
    const pastaAtual = this.obterPastaAtual();
    pastaAtual
      ? this.abrirPasta(pastaAtual, false)
      : this.carregarConteudoRaiz();
  }

  carregarConteudoRaiz(): void {
    this.loading = true;
    this.adminService
      .listarConteudoRaiz(this.ordenarPor, this.ordemAsc, this.filtroNome)
      .subscribe({
        next: (pastas) => {
          this.pastas = pastas;
          this.arquivos = [];
          this.breadcrumb = [];
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao carregar raiz', err),
      });
  }

  abrirPasta(pasta: PastaCompletaDTO, adicionarBreadcrumb = true): void {
    if (!pasta) return;
    if (adicionarBreadcrumb) this.breadcrumb.push(pasta);

    this.loading = true;
    this.adminService
      .getPastaPorId(pasta.id, this.ordenarPor, this.ordemAsc, this.filtroNome)
      .subscribe({
        next: (detalhada) => {
          this.pastas = detalhada.subPastas ?? [];
          this.arquivos = detalhada.arquivos ?? [];
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao abrir pasta', err),
      });
  }

  navegarPara(index: number): void {
    if (index < 0) return this.carregarConteudoRaiz();
    this.breadcrumb = this.breadcrumb.slice(0, index + 1);
    this.recarregarConteudo();
  }

  voltarUmNivel(): void {
    this.navegarPara(this.breadcrumb.length - 2);
  }

  public obterPastaAtual(): PastaCompletaDTO | undefined {
    return this.breadcrumb[this.breadcrumb.length - 1];
  }

  // ---------------- Criação de pasta ----------------
  abrirModalCriarPasta(): void {
    this.modalCriarPastaAberto = true;
    this.novoNomePasta = '';
    this.usuariosSelecionados = [];
  }
  fecharModalCriarPasta(): void {
    this.modalCriarPastaAberto = false;
  }
  criarPasta(): void {
    if (!this.novoNomePasta.trim()) return;

    const body = {
      nome: this.novoNomePasta.trim(),
      pastaPaiId: this.obterPastaAtual()?.id,
      usuariosComPermissaoIds:
        this.usuariosSelecionados.length > 0
          ? this.usuariosSelecionados.map((u) => u.id)
          : [],
    };

    this.adminService.criarPasta(body).subscribe({
      next: () => {
        this.toastService.showSuccess('Pasta criada com sucesso!');
        this.recarregarConteudo();
        this.fecharModalCriarPasta();
      },
      error: (err) => this.handleError('Erro ao criar pasta', err),
    });
  }

  // ---------------- Renomear ----------------
  abrirModalRenomear(item: PastaCompletaDTO | ArquivoAdmin): void {
    this.itemParaRenomear = item;
    this.novoNomeItem = this.isPasta(item) ? item.nomePasta : item.nome;
    this.modalRenomearAberto = true;
  }
  fecharModalRenomear(): void {
    this.modalRenomearAberto = false;
    this.itemParaRenomear = null;
    this.novoNomeItem = '';
  }
  renomearItem(): void {
    if (!this.itemParaRenomear) return;
    const novoNome = this.novoNomeItem.trim();
    if (!novoNome) return;

    const nomeAtual = this.isPasta(this.itemParaRenomear)
      ? (this.itemParaRenomear.nomePasta || '').trim()
      : (this.itemParaRenomear.nome || '').trim();

    if (nomeAtual.toLowerCase() === novoNome.toLowerCase()) {
      this.toastService.showInfo('O nome não foi alterado.');
      this.fecharModalRenomear();
      return;
    }

    const req: Observable<any> = this.isPasta(this.itemParaRenomear)
      ? this.adminService.renomearPasta(this.itemParaRenomear.id, novoNome)
      : this.adminService.renomearArquivo(this.itemParaRenomear.id, novoNome);

    this.loading = true;
    req.subscribe({
      next: () => {
        this.toastService.showSuccess('Nome alterado com sucesso!');
        this.recarregarConteudo();
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao renomear', err),
    });

    this.fecharModalRenomear();
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
      const novos = Array.from(input.files).filter(
        (novo) =>
          !this.arquivosParaUpload.some(
            (existente) =>
              existente.name === novo.name && existente.size === novo.size
          )
      );
      this.arquivosParaUpload.push(...novos);
    }
  }
  removerArquivoSelecionado(file: File): void {
    this.arquivosParaUpload = this.arquivosParaUpload.filter((f) => f !== file);
  }
  uploadArquivos(): void {
    const pastaAtual = this.obterPastaAtual();
    if (!pastaAtual) return this.handleError('Selecione uma pasta.');
    if (!this.arquivosParaUpload.length) return;

    this.loading = true;
    this.adminService
      .uploadMultiplosArquivos(this.arquivosParaUpload, pastaAtual.id)
      .subscribe({
        next: () => {
          this.recarregarConteudo();
          this.fecharModalUpload();
          this.loading = false;
          this.arquivosParaUpload = [];
        },
        error: (err) => this.handleError('Erro ao fazer upload', err),
      });
  }

  // ---------------- Seleção ----------------
  isSelecionado(item: PastaCompletaDTO | ArquivoAdmin): boolean {
    return this.itensSelecionados.includes(item);
  }
  toggleSelecao(item: PastaCompletaDTO | ArquivoAdmin): void {
    this.isSelecionado(item)
      ? (this.itensSelecionados = this.itensSelecionados.filter(
          (i) => i !== item
        ))
      : this.itensSelecionados.push(item);
  }
  get todosItens(): (PastaCompletaDTO | ArquivoAdmin)[] {
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
      (i) => !this.isSelecionado(i)
    );
  }

  // ---------------- Exclusão ----------------
  abrirModalExcluir(item: PastaCompletaDTO | ArquivoAdmin): void {
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
      ? this.adminService.excluirPasta(this.itemParaExcluir.id)
      : this.adminService.excluirArquivo(this.itemParaExcluir.id);

    this.loading = true;
    request.subscribe({
      next: () => {
        this.toastService.showSuccess('Exclusão feita com sucesso!');
        this.recarregarConteudo();
        this.fecharModalExcluir();
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao excluir item', err),
    });
  }

  abrirModalExcluirSelecionados(): void {
    if (this.itensSelecionados.length > 0)
      this.modalExcluirSelecionadosAberto = true;
  }
  fecharModalExcluirSelecionados(): void {
    this.modalExcluirSelecionadosAberto = false;
  }
  confirmarExclusaoSelecionados(): void {
    if (!this.itensSelecionados.length) return;

    const pastasSelecionadas = this.itensSelecionados.filter(this.isPasta);
    const arquivosSelecionados = this.itensSelecionados.filter(
      (i) => !this.isPasta(i)
    );

    const requests: Observable<any>[] = [];

    if (pastasSelecionadas.length) {
      const dto: PastaExcluirDTO = {
        idsPastas: pastasSelecionadas.map((p) => p.id),
        excluirConteudo: true,
      };
      requests.push(
        this.adminService.excluirPastasEmLote(dto).pipe(
          catchError((err) => {
            this.handleError('Erro ao excluir pastas em lote', err);
            return of(null);
          })
        )
      );
    }

    arquivosSelecionados.forEach((arquivo) =>
      requests.push(
        this.adminService.excluirArquivo((arquivo as ArquivoAdmin).id).pipe(
          catchError((err) => {
            this.handleError('Erro ao excluir arquivo', err);
            return of(null);
          })
        )
      )
    );

    forkJoin(requests).subscribe({
      next: () => {
        this.toastService.showSuccess('Exclusão feita com sucesso!');
        this.itensSelecionados = [];
        this.recarregarConteudo();
        this.fecharModalExcluirSelecionados();
      },
    });
  }

  // ---------------- Copiar ----------------
  abrirModalCopiar(arquivo: ArquivoAdmin): void {
    this.arquivoParaCopiar = arquivo;
    this.modalCopiarAberto = true;
    this.pastaDestinoSelecionada = null;

    this.adminService.listarConteudoRaiz().subscribe({
      next: (pastas) => (this.pastasDisponiveisParaCopiar = pastas),
    });
  }
  fecharModalCopiar(): void {
    this.modalCopiarAberto = false;
    this.arquivoParaCopiar = null;
    this.pastaDestinoSelecionada = null;
    this.pastasDisponiveisParaCopiar = [];
  }
  copiarArquivo(): void {
    if (!this.arquivoParaCopiar || !this.pastaDestinoSelecionada) return;
    this.loading = true;
    this.adminService
      .copiarArquivo(this.arquivoParaCopiar.id, this.pastaDestinoSelecionada.id)
      .subscribe({
        next: () => {
          this.recarregarConteudo();
          this.fecharModalCopiar();
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao copiar arquivo', err),
      });
  }

  abrirModalCopiarPasta(pasta: PastaCompletaDTO): void {
    this.pastaParaCopiar = pasta;
    this.modalCopiarPastaAberto = true;
    this.pastaDestinoSelecionada = null;

    this.adminService.listarConteudoRaiz().subscribe({
      next: (pastas) => {
        this.pastasDisponiveisParaCopiar = pastas.filter(
          (p) => p.id !== pasta.id
        );
      },
    });
  }
  fecharModalCopiarPasta(): void {
    this.modalCopiarPastaAberto = false;
    this.pastaParaCopiar = null;
    this.pastaDestinoSelecionada = null;
    this.pastasDisponiveisParaCopiar = [];
  }
  copiarPasta(): void {
    if (!this.pastaParaCopiar) return;
    const destinoId = this.pastaDestinoSelecionada?.id;

    this.loading = true;
    this.adminService
      .copiarPasta(this.pastaParaCopiar.id, destinoId)
      .subscribe({
        next: () => {
          this.recarregarConteudo();
          this.fecharModalCopiarPasta();
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao copiar pasta', err),
      });
  }

  // ---------------- Mover ----------------
  abrirModalMoverPasta(pasta: PastaCompletaDTO): void {
    this.pastaParaMover = pasta;
    this.modalMoverPastaAberto = true;
    this.pastaDestinoMover = null;

    this.adminService.listarConteudoRaiz().subscribe({
      next: (pastas) => {
        this.pastasDisponiveisParaMover = pastas.filter(
          (p) => p.id !== pasta.id
        );
      },
    });
  }
  fecharModalMoverPasta(): void {
    this.modalMoverPastaAberto = false;
    this.pastaParaMover = null;
    this.pastaDestinoMover = null;
    this.pastasDisponiveisParaMover = [];
  }

  moverPasta(): void {
    if (!this.pastaParaMover) return;
    const destinoId = this.pastaDestinoMover
      ? this.pastaDestinoMover.id
      : undefined;

    this.loading = true;
    this.adminService.moverPasta(this.pastaParaMover.id, destinoId).subscribe({
      next: () => {
        this.recarregarConteudo();
        this.fecharModalMoverPasta();
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao mover pasta', err),
    });
  }

  abrirModalMoverArquivo(arquivo: ArquivoAdmin): void {
    this.arquivoParaMover = arquivo;
    this.modalMoverArquivoAberto = true;
    this.pastaDestinoArquivo = null;

    // Carrega todas as pastas a partir da raiz
    this.adminService.listarConteudoRaiz().subscribe({
      next: (pastas) => {
        // 🔹 Transforma em lista plana (raiz + filhos + netos)
        this.pastasDisponiveisParaMover = this.flattenPastas(pastas);

        // 🔹 Remove a pasta atual para evitar mover para o mesmo lugar
        const pastaAtual = this.obterPastaAtual();
        if (pastaAtual) {
          this.pastasDisponiveisParaMover =
            this.pastasDisponiveisParaMover.filter(
              (p) => p.id !== pastaAtual.id
            );
        }
      },
    });
  }

  fecharModalMoverArquivo(): void {
    this.modalMoverArquivoAberto = false;
    this.arquivoParaMover = null;
    this.pastaDestinoArquivo = null;
    this.pastasDisponiveisParaMover = [];
  }
  moverArquivo(): void {
    if (!this.arquivoParaMover || !this.pastaDestinoArquivo?.id) return;

    console.log(
      'Mover arquivo:',
      this.arquivoParaMover.id,
      '→',
      this.pastaDestinoArquivo.id
    );

    if (this.pastaDestinoArquivo.id === this.pastaParaMover?.id) {
      this.toastService.showInfo('O arquivo já está nesta pasta.');
      this.fecharModalMoverArquivo();
      return;
    }

    this.loading = true;
    this.adminService
      .moverArquivo(this.arquivoParaMover.id, this.pastaDestinoArquivo.id)
      .subscribe({
        next: () => {
          this.toastService.showSuccess('Arquivo movido com sucesso!');
          this.recarregarConteudo();
          this.fecharModalMoverArquivo();
          this.loading = false;
        },
        error: (err) => this.handleError('Erro ao mover arquivo', err),
      });
  }

  // ---------------- Arquivo ----------------
  abrirOpcoesArquivo(arquivo: ArquivoAdmin): void {
    this.arquivoSelecionado = arquivo;
    this.modalOpcoesArquivoAberto = true;
  }
  fecharModalOpcoesArquivo(): void {
    this.modalOpcoesArquivoAberto = false;
    this.arquivoSelecionado = null;
  }
  abrirArquivo(arquivo: ArquivoAdmin | null) {
    if (!arquivo) return;
    this.adminService.abrirArquivo(arquivo.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
    this.fecharModalOpcoesArquivo();
  }
  downloadArquivo(arquivo: ArquivoAdmin | null) {
    if (!arquivo) return;
    this.adminService.downloadArquivo(arquivo.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = arquivo.nome;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }

  // ---------------- Permissões ----------------
  abrirModalPermissao(pasta: PastaCompletaDTO): void {
    this.pastaParaPermissao = pasta;
    this.loading = true;

    this.adminService.listarUsuariosPorPasta(pasta.id).subscribe({
      next: (usuarios) => {
        this.usuariosComPermissao = usuarios;
        this.usuariosIniciaisComPermissao = [...usuarios];
        this.atualizarListaDeDisponiveis();
        this.modalPermissaoAberto = true;
        this.loading = false;
      },
      error: (err) => {
        this.handleError('Erro ao carregar usuários da pasta', err);
        this.loading = false;
      },
    });
  }
  fecharModalPermissao(): void {
    this.modalPermissaoAberto = false;
    this.pastaParaPermissao = null;
    this.usuariosComPermissao = [];
    this.usuariosDisponiveis = [];
    this.usuariosIniciaisComPermissao = [];
  }
  abrirModalSelecionarUsuario(modal: 'criarPasta' | 'permissao') {
    this.modalAtivo = modal;
    this.usuariosExcluidosIds =
      modal === 'criarPasta'
        ? this.usuariosSelecionados.map((u) => u.id)
        : this.usuariosComPermissao.map((u) => u.id);
    this.modalSelecionarUsuarioAberto = true;
  }
  onUsuarioSelecionado(usuario: Usuario | null) {
    if (usuario) {
      if (this.modalAtivo === 'criarPasta') {
        this.usuariosSelecionados.push(usuario);
      } else if (this.modalAtivo === 'permissao') {
        this.usuariosComPermissao.push({
          id: usuario.id,
          username: usuario.username,
          nome: usuario.nome,
        });
        this.atualizarListaDeDisponiveis();
      }
    }
    this.modalSelecionarUsuarioAberto = false;
    this.modalAtivo = null;
  }
  removerUsuarioSelecionado(usuario: Usuario) {
    this.usuariosSelecionados = this.usuariosSelecionados.filter(
      (u) => u.id !== usuario.id
    );
  }
  removerUsuarioPermissao(usuario: UsuarioResumoDTO): void {
    this.usuariosComPermissao = this.usuariosComPermissao.filter(
      (u) => u.id !== usuario.id
    );
    this.atualizarListaDeDisponiveis();
  }
  private atualizarListaDeDisponiveis(): void {
    this.usuariosDisponiveis = this.usuarios.filter(
      (u) => !this.usuariosComPermissao.some((p) => p.id === u.id)
    );
    this.usuariosComPermissaoIds = this.usuariosComPermissao.map((u) => u.id);
  }
  atualizarPermissoes(): void {
    if (!this.pastaParaPermissao || !this.pastaParaPermissao.id) {
      this.toastService.showError('ID da pasta não encontrado.');
      return;
    }

    const idsAtuais = new Set(this.usuariosComPermissao.map((u) => u.id));
    const idsIniciais = new Set(
      this.usuariosIniciaisComPermissao.map((u) => u.id)
    );

    const adicionarUsuariosIds = this.usuariosComPermissao
      .filter((u) => !idsIniciais.has(u.id))
      .map((u) => u.id);

    const removerUsuariosIds = this.usuariosIniciaisComPermissao
      .filter((u) => !idsAtuais.has(u.id))
      .map((u) => u.id);

    if (adicionarUsuariosIds.length === 0 && removerUsuariosIds.length === 0) {
      this.toastService.showInfo('Nenhuma alteração de permissão foi feita.');
      this.fecharModalPermissao();
      return;
    }

    const dto: PastaPermissaoAcaoDTO = {
      pastaId: this.pastaParaPermissao.id,
      adicionarUsuariosIds,
      removerUsuariosIds,
    };

    this.loading = true;
    this.adminService.atualizarPermissoesAcao(dto).subscribe({
      next: () => {
        this.toastService.showSuccess('Permissões atualizadas com sucesso!');
        this.recarregarConteudo();
        this.fecharModalPermissao();
        this.loading = false;
      },
      error: (err) => this.handleError('Erro ao atualizar permissões', err),
    });
  }

  // ---------------- Helpers ----------------
  isPasta(item: PastaCompletaDTO | ArquivoAdmin): item is PastaCompletaDTO {
    return (item as PastaCompletaDTO).subPastas !== undefined;
  }
  getNomeItem(item: PastaCompletaDTO | ArquivoAdmin | null): string {
    return item ? (this.isPasta(item) ? item.nomePasta : item.nome) : '';
  }

  private handleError(mensagemPadrao: string, err?: any): void {
    console.error(mensagemPadrao, err);

    let errorMessage = mensagemPadrao;

    if (
      err &&
      err.error &&
      typeof err.error === 'object' &&
      'status' in err.error
    ) {
      const backend = err.error as ErrorMessage;
      errorMessage = `Erro (${backend.status}${
        backend.error ? ' - ' + backend.error : ''
      }): ${backend.message || mensagemPadrao}`;
    } else if (err && typeof err === 'object' && 'status' in err) {
      const backend = err as ErrorMessage;
      errorMessage = `Erro (${backend.status}${
        backend.error ? ' - ' + backend.error : ''
      }): ${backend.message || mensagemPadrao}`;
    }

    this.toastService.showError(errorMessage);
    this.loading = false;
  }

  private flattenPastas(
    pastas: PastaCompletaDTO[],
    prefix: string = ''
  ): PastaCompletaDTO[] {
    let lista: PastaCompletaDTO[] = [];

    for (const pasta of pastas) {
      // adiciona a pasta atual (prefix ajuda a visualizar hierarquia no select)
      lista.push({
        ...pasta,
        nomePasta: prefix + pasta.nomePasta,
      });

      // se tiver subpastas, adiciona recursivamente
      if (pasta.subPastas && pasta.subPastas.length > 0) {
        lista = lista.concat(
          this.flattenPastas(pasta.subPastas, prefix + '— ')
        );
      }
    }

    return lista;
  }

  // 🔹 Helper opcional para coletar arquivos em toda árvore
  private coletarArquivosRecursivo(pastas: any[]): any[] {
    let arquivos: any[] = [];
    for (const pasta of pastas) {
      if (pasta.arquivos && pasta.arquivos.length > 0) {
        arquivos.push(...pasta.arquivos);
      }
      if (pasta.subPastas && pasta.subPastas.length > 0) {
        arquivos.push(...this.coletarArquivosRecursivo(pasta.subPastas));
      }
    }

    console.log('Arquivos encontrados:', arquivos);
    return arquivos;
  }

  private carregarUsuarios(): void {
    this.adminService.listarUsuarios().subscribe({
      next: (resposta: Paginacao<Usuario>) => {
        this.usuarios = resposta.content || [];
      },
      error: (err: any) => {
        this.handleError('Erro ao carregar usuários', err);
      },
    });
  }
}
