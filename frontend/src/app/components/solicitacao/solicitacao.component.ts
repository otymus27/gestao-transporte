import { Component, inject, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// MDB Angular
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import {
  MdbModalModule,
  MdbModalRef,
  MdbModalService,
} from 'mdb-angular-ui-kit/modal';

import { AuthService } from '../../services/auth.service';

// Models e Services
import { ToastService } from '../../services/toast.service';
import { Paginacao } from '../../models/paginacao';
import {
  Solicitacao,
  StatusSolicitacao,
  novaSolicitacao,
  toSolicitacaoRequest,
} from '../../models/solicitacao';
import {
  ErrorMessage,
  SolicitacaoService,
} from '../../services/solicitacao.service';

import { Carro } from '../../models/carro';
import { Motorista } from '../../models/motorista';
import { Usuario } from '../../models/usuario';
import { Setor } from '../../models/setor';
import { Destino } from '../../models/destino';

import { CarroService } from '../../services/carro.service';
import { MotoristaService } from '../../services/motorista.service';
import { UsuarioService } from '../../services/usuario.service';
import { SetorService } from '../../services/setor.service';
import { DestinoService } from '../../services/destino.service';

@Component({
  selector: 'app-solicitacao',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './solicitacao.component.html',
  styleUrl: './solicitacao.component.scss',
})
export class SolicitacaoComponent {
  lista: Solicitacao[] = [];
  registroSelecionado!: Solicitacao;
  statusOptions: StatusSolicitacao[] = [
    'PENDENTE',
    'EM_ANDAMENTO',
    'CONCLUIDO',
    'CANCELADO',
  ];

  isAdmin = false;

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtro
  filtroStatus: string = '';
  filtroId: string = '';
  filtroUsuarioLogin: string = '';

  // Ordenação
  colunaOrdenada: keyof Solicitacao = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  // Injeções
  solicitacaoService = inject(SolicitacaoService);
  carroService = inject(CarroService);
  motoristaService = inject(MotoristaService);
  usuarioService = inject(UsuarioService);
  setorService = inject(SetorService);
  destinoService = inject(DestinoService);
  modalService = inject(MdbModalService);
  toastService = inject(ToastService);
  authService = inject(AuthService);

  // Listas
  carros: Carro[] = [];
  motoristas: Motorista[] = [];
  usuarios: Usuario[] = [];
  setores: Setor[] = [];
  destinos: Destino[] = [];

  // Campos exibidos no formulário
  motoristaNome = '';
  carroPlaca = '';
  setorNome = '';
  destinoNome = '';

  // Modais
  @ViewChild('modalSolicitacaoAbertura')
  modalSolicitacaoAbertura!: TemplateRef<any>;

  @ViewChild('modalSolicitacaoFechamento')
  modalSolicitacaoFechamento!: TemplateRef<any>;

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  modalRef!: MdbModalRef<any>;

  constructor() {
    this.listar();
    this.carregarCombos();

    // ✅ Detecta se o logado é admin
    const roles = this.authService.getLoggedInRoles();
    this.isAdmin = roles.includes('ADMIN');
  }

  carregarCombos() {
    this.usuarioService
      .listar(0, 100)
      .subscribe((res) => (this.usuarios = res.content));
  }

  // 📌 Flag de filtro rápido
  mostrarSomentePendentes = false;

  alternarFiltroPendentes() {
    this.mostrarSomentePendentes = !this.mostrarSomentePendentes;
    this.page = 0; // sempre volta pra primeira página
    if (this.mostrarSomentePendentes) {
      this.filtroStatus = 'PENDENTE';
    } else {
      this.filtroStatus = '';
    }
    this.listar();
  }

  // --- Motorista ---
  buscarMotoristas(query: string) {
    if (!query.trim()) {
      this.motoristas = [];
      return;
    }
    this.motoristaService.filtrarPorNome(query).subscribe({
      next: (res) => (this.motoristas = res.content),
      error: () => this.toastService.showError('Erro ao buscar motoristas!'),
    });
  }
  selecionarMotorista(m: Motorista) {
    this.registroSelecionado.motoristaId = m.id!;
    this.motoristaNome = m.nome;
    this.motoristas = [];
  }

  // --- Carro ---
  buscarCarros(query: string) {
    if (!query.trim()) {
      this.carros = [];
      return;
    }
    this.carroService.filtrar(query).subscribe({
      next: (res) => (this.carros = res.content),
      error: () => this.toastService.showError('Erro ao buscar carros!'),
    });
  }
  selecionarCarro(c: Carro) {
    this.registroSelecionado.carroId = c.id!;
    this.carroPlaca = c.placa;
    this.carros = [];
  }

  // --- Setor ---
  buscarSetores(query: string) {
    if (!query.trim()) {
      this.setores = [];
      return;
    }
    this.setorService.filtrarPorNome(query).subscribe({
      next: (res) => (this.setores = res.content),
      error: () => this.toastService.showError('Erro ao buscar setores!'),
    });
  }
  selecionarSetor(s: Setor) {
    this.registroSelecionado.setorId = s.id!;
    this.setorNome = s.nome;
    this.setores = [];
  }

  // --- Destino ---
  buscarDestinos(query: string) {
    if (!query.trim()) {
      this.destinos = [];
      return;
    }
    this.destinoService.filtrarPorNome(query).subscribe({
      next: (res) => (this.destinos = res.content),
      error: () => this.toastService.showError('Erro ao buscar destinos!'),
    });
  }
  selecionarDestino(d: Destino) {
    this.registroSelecionado.destinoId = d.id!;
    this.destinoNome = d.nome;
    this.destinos = [];
  }

  // 📌 Listar com filtros
  listar() {
    const filtros: any = {};

    if (this.filtroId && this.filtroId.trim() !== '') {
      filtros.id = +this.filtroId;
    }
    if (this.filtroUsuarioLogin && this.filtroUsuarioLogin.trim() !== '') {
      filtros.usuarioLogin = this.filtroUsuarioLogin;
    }
    if (this.filtroStatus && this.filtroStatus.trim() !== '') {
      filtros.status = this.filtroStatus;
    }

    const service$ =
      Object.keys(filtros).length > 0
        ? this.solicitacaoService.filtrarGenerico(filtros, this.page, this.size)
        : this.solicitacaoService.listar(
            this.page,
            this.size,
            this.colunaOrdenada,
            this.ordem
          );

    service$.subscribe({
      next: (res: Paginacao<Solicitacao>) => {
        this.lista = res.content;
        this.page = res.number;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
      },
      error: () =>
        this.toastService.showError('Erro ao carregar solicitações!'),
    });
  }

  // 📌 Paginação
  irParaPagina(p: number) {
    this.page = p;
    this.listar();
  }
  proximaPagina() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.listar();
    }
  }
  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.listar();
    }
  }

  // 📌 Filtros
  aplicarFiltros() {
    this.page = 0;
    this.listar();
  }
  limparFiltros() {
    this.filtroStatus = '';
    this.aplicarFiltros();
  }

  // 📌 Ordenação
  ordenarPor(campo: keyof Solicitacao) {
    if (this.colunaOrdenada === campo) {
      this.ordem = this.ordem === 'asc' ? 'desc' : 'asc';
    } else {
      this.colunaOrdenada = campo;
      this.ordem = 'asc';
    }
    this.listar();
  }

  // 📌 Modal de Abertura
  abrirModalAbertura() {
    this.registroSelecionado = novaSolicitacao();
    this.registroSelecionado.usuarioId = this.authService.getUsuarioId();
    this.registroSelecionado.status = 'PENDENTE'; // ✅ já seta como PENDENTE

    this.motoristaNome = '';
    this.carroPlaca = '';
    this.setorNome = '';
    this.destinoNome = '';

    this.modalRef = this.modalService.open(this.modalSolicitacaoAbertura);
  }

  // 📌 Modal de Fechamento ou Edição
  abrirModalFechamento(solicitacao: Solicitacao) {
    this.registroSelecionado = { ...solicitacao };

    if (!this.isAdmin) {
      this.registroSelecionado.status = 'CONCLUIDO';
    }

    this.motoristaNome = solicitacao.motoristaNome ?? '';
    this.carroPlaca = solicitacao.carroPlaca ?? '';
    this.setorNome = solicitacao.setorNome ?? '';
    this.destinoNome = solicitacao.destinoNome ?? '';

    this.modalRef = this.modalService.open(this.modalSolicitacaoFechamento);
  }
  // 📌 Modal de Edição (se você quiser manter separado de fechamento)
  editarModal(solicitacao: Solicitacao) {
    this.registroSelecionado = {
      ...solicitacao,
      dataSolicitacao: solicitacao.dataSolicitacao
        ? this.formatarDataParaInput(solicitacao.dataSolicitacao)
        : new Date().toISOString().slice(0, 16),
    };

    // ✅ Aqui também preenche
    this.motoristaNome = solicitacao.motoristaNome ?? '';
    this.carroPlaca = solicitacao.carroPlaca ?? '';
    this.setorNome = solicitacao.setorNome ?? '';
    this.destinoNome = solicitacao.destinoNome ?? '';

    this.modalRef = this.modalService.open(this.modalSolicitacaoAbertura);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar Abertura
  salvarSolicitacao(solicitacao: Solicitacao) {
    const payload = toSolicitacaoRequest(solicitacao);
    const isNovo = !solicitacao.id || solicitacao.id <= 0;

    const acao = isNovo
      ? this.solicitacaoService.cadastrar(payload)
      : this.solicitacaoService.atualizar(solicitacao.id, payload);

    acao.subscribe({
      next: () => {
        this.toastService.showSuccess(
          `Solicitação ${isNovo ? 'aberta' : 'atualizada'} com sucesso!`
        );
        this.listar();
        this.modalRef.close();
      },
      error: (err: ErrorMessage) =>
        this.toastService.showError(
          `Erro (${err.status} - ${err.error}): ${err.message}`
        ),
    });
  }

  // 📌 Salvar Fechamento
  fecharSolicitacao(solicitacao: Solicitacao) {
    const payload = toSolicitacaoRequest(solicitacao);
    payload.status = 'CONCLUIDO';

    this.solicitacaoService.atualizar(solicitacao.id, payload).subscribe({
      next: () => {
        this.toastService.showSuccess('Solicitação concluída com sucesso!');
        this.listar();
        this.modalRef.close();
      },
      error: (err: ErrorMessage) =>
        this.toastService.showError(
          `Erro (${err.status} - ${err.error}): ${err.message}`
        ),
    });
  }

  // 📌 Excluir
  excluir(solicitacao: Solicitacao) {
    this.registroSelecionado = solicitacao;
    this.modalRef = this.modalService.open(this.modalConfirmacaoExclusao);
  }
  excluirConfirmado() {
    this.modalRef.close();
    this.solicitacaoService.excluir(this.registroSelecionado.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Solicitação excluída com sucesso!');
        this.listar();
      },
      error: (err: ErrorMessage) =>
        this.toastService.showError(
          `Erro (${err.status} - ${err.error}): ${err.message}`
        ),
    });
  }

  /** Converte string ISO para datetime-local (yyyy-MM-ddTHH:mm) */
  private formatarDataParaInput(data: string | Date): string {
    const d = new Date(data);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(
      d.getDate()
    )}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
