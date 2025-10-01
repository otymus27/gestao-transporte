import { CommonModule } from '@angular/common';
import { Component, inject, TemplateRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import {
  MdbModalModule,
  MdbModalRef,
  MdbModalService,
} from 'mdb-angular-ui-kit/modal';

import { ToastService } from '../../services/toast.service';
import { Paginacao } from '../../models/paginacao';
import {
  Solicitacao,
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

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtro
  filtroStatus: string = '';

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

  // Listas para selects
  carros: Carro[] = [];
  motoristas: Motorista[] = [];
  usuarios: Usuario[] = [];
  setores: Setor[] = [];
  destinos: Destino[] = [];

  // Modais
  @ViewChild('modalSolicitacaoDetalhe')
  modalSolicitacaoDetalhe!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  constructor() {
    this.listar();
    this.carregarCombos();
  }

  carregarCombos() {
    this.carroService
      .listar(0, 100)
      .subscribe((res) => (this.carros = res.content));
    this.motoristaService
      .listar(0, 100)
      .subscribe((res) => (this.motoristas = res.content));
    this.usuarioService
      .listar(0, 100)
      .subscribe((res) => (this.usuarios = res.content));
    this.setorService
      .listar(0, 100)
      .subscribe((res) => (this.setores = res.content));
    this.destinoService
      .listar(0, 100)
      .subscribe((res) => (this.destinos = res.content));
  }

  // 📌 Listar
  listar() {
    if (this.filtroStatus?.trim()) {
      this.solicitacaoService
        .filtrarPorStatus(this.filtroStatus, this.page, this.size)
        .subscribe({
          next: (resposta: Paginacao<Solicitacao>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () =>
            this.toastService.showError('Erro ao buscar solicitações!'),
        });
    } else {
      this.solicitacaoService
        .listar(this.page, this.size, this.colunaOrdenada, this.ordem)
        .subscribe({
          next: (resposta: Paginacao<Solicitacao>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () =>
            this.toastService.showError('Erro ao listar solicitações!'),
        });
    }
  }

  // 📌 Métodos de paginação
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

  // 📌 Modal de cadastro
  cadastrarModal() {
    this.registroSelecionado = novaSolicitacao();
    this.modalRef = this.modalService.open(this.modalSolicitacaoDetalhe);
  }

  // 📌 Modal de edição
  editarModal(solicitacao: Solicitacao) {
    this.registroSelecionado = {
      ...solicitacao,
      carroId: solicitacao.carroId ?? (solicitacao as any).carro?.id ?? null,
      motoristaId:
        solicitacao.motoristaId ?? (solicitacao as any).motorista?.id ?? null,
      usuarioId:
        solicitacao.usuarioId ?? (solicitacao as any).usuario?.id ?? null,
      setorId: solicitacao.setorId ?? (solicitacao as any).setor?.id ?? null,
      destinoId:
        solicitacao.destinoId ?? (solicitacao as any).destino?.id ?? null,
      dataSolicitacao: solicitacao.dataSolicitacao
        ? this.formatarDataParaInput(solicitacao.dataSolicitacao)
        : new Date().toISOString().slice(0, 16),
    };
    this.modalRef = this.modalService.open(this.modalSolicitacaoDetalhe);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar
  salvarSolicitacao(solicitacao: Solicitacao) {
    if (!solicitacao.status?.trim()) {
      this.toastService.showError('O campo status é obrigatório.');
      return;
    }

    const payload = toSolicitacaoRequest(solicitacao);
    const isNovo = !solicitacao.id || solicitacao.id <= 0;

    if (isNovo) {
      this.solicitacaoService.cadastrar(payload).subscribe({
        next: () => {
          this.toastService.showSuccess('Solicitação cadastrada com sucesso!');
          this.listar();
          this.modalRef.close();
        },
        error: (err: ErrorMessage) =>
          this.toastService.showError(
            `Erro (${err.status} - ${err.error}): ${err.message}`
          ),
      });
    } else {
      this.solicitacaoService.atualizar(solicitacao.id, payload).subscribe({
        next: () => {
          this.toastService.showSuccess('Solicitação atualizada com sucesso!');
          this.listar();
          this.modalRef.close();
        },
        error: (err: ErrorMessage) =>
          this.toastService.showError(
            `Erro (${err.status} - ${err.error}): ${err.message}`
          ),
      });
    }
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

  /** Converte string ISO para formato datetime-local (yyyy-MM-ddTHH:mm) */
  private formatarDataParaInput(data: string | Date): string {
    const d = new Date(data);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(
      d.getDate()
    )}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
