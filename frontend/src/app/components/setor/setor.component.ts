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
import { Setor } from '../../models/setor';
import { ErrorMessage, SetorService } from '../../services/setor.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-setor',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './setor.component.html',
  styleUrl: './setor.component.scss',
})
export class SetorComponent {
  lista: Setor[] = [];
  registroSelecionado!: Setor;

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtro
  filtroNome: string = '';

  // Ordenação
  colunaOrdenada: keyof Setor = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  // Injeções
  setorService = inject(SetorService);
  modalService = inject(MdbModalService);
  toastService = inject(ToastService);
  private authService = inject(AuthService);

  // Modais
  @ViewChild('modalSetorDetalhe') modalSetorDetalhe!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  canDelete(): boolean {
    return this.authService.getLoggedInRoles().includes('ADMIN');
  }

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  constructor() {
    this.listar();
  }

  // 📌 Listar setores
  listar() {
    if (this.filtroNome?.trim()) {
      this.setorService
        .filtrarPorNome(
          this.filtroNome,
          this.page,
          this.size,
          this.colunaOrdenada,
          this.ordem
        )
        .subscribe({
          next: (resposta: Paginacao<Setor>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao buscar setores!'),
        });
    } else {
      this.setorService
        .listar(this.page, this.size, this.colunaOrdenada, this.ordem)
        .subscribe({
          next: (resposta: Paginacao<Setor>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao listar setores!'),
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
    this.filtroNome = '';
  }

  limparFiltros() {
    this.filtroNome = '';
    this.aplicarFiltros();
  }

  // 📌 Ordenação
  ordenarPor(campo: keyof Setor) {
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
    this.registroSelecionado = { id: 0, nome: '' };
    this.modalRef = this.modalService.open(this.modalSetorDetalhe);
  }

  // 📌 Modal de edição
  editarModal(setor: Setor) {
    this.registroSelecionado = { ...setor };
    this.modalRef = this.modalService.open(this.modalSetorDetalhe);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar (cadastrar ou atualizar)
  salvarSetor(setor: Setor) {
    if (!setor.nome?.trim()) {
      this.toastService.showError('O campo nome é obrigatório.');
      return;
    }

    const isNovo = !setor.id || setor.id <= 0;

    if (isNovo) {
      this.setorService.cadastrar(setor).subscribe({
        next: () => {
          this.toastService.showSuccess('Setor cadastrado com sucesso!');
          this.listar();
          this.modalRef.close();
        },
        error: (err: ErrorMessage) => {
          this.toastService.showError(
            `Erro (${err.status} - ${err.error}): ${err.message}`
          );
        },
      });
    } else {
      this.setorService.atualizar(setor.id, setor).subscribe({
        next: () => {
          this.toastService.showSuccess('Setor atualizado com sucesso!');
          this.listar();
          this.modalRef.close();
        },
        error: (err: ErrorMessage) => {
          this.toastService.showError(
            `Erro (${err.status} - ${err.error}): ${err.message}`
          );
        },
      });
    }
  }

  // 📌 Excluir setor
  excluir(setor: Setor) {
    this.registroSelecionado = setor;
    this.modalRef = this.modalService.open(this.modalConfirmacaoExclusao);
  }

  excluirConfirmado() {
    this.modalRef.close();
    this.setorService.excluir(this.registroSelecionado.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Setor excluído com sucesso!');
        this.listar();
      },
      error: (err: ErrorMessage) => {
        this.toastService.showError(
          `Erro (${err.status} - ${err.error}): ${err.message}`
        );
      },
    });
  }
}
