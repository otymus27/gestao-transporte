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
import { Destino } from '../../models/destino';
import { ErrorMessage, DestinoService } from '../../services/destino.service';

@Component({
  selector: 'app-destino',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './destino.component.html',
  styleUrl: './destino.component.scss',
})
export class DestinoComponent {
  lista: Destino[] = [];
  registroSelecionado!: Destino;

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtro
  filtroNome: string = '';

  // Ordenação
  colunaOrdenada: keyof Destino = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  // Injeções
  destinoService = inject(DestinoService);
  modalService = inject(MdbModalService);
  toastService = inject(ToastService);

  // Modais
  @ViewChild('modalDestinoDetalhe') modalDestinoDetalhe!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  constructor() {
    this.listar();
  }

  // 📌 Listar destinos
  listar() {
    if (this.filtroNome?.trim()) {
      this.destinoService
        .filtrarPorNome(
          this.filtroNome,
          this.page,
          this.size,
          this.colunaOrdenada,
          this.ordem
        )
        .subscribe({
          next: (resposta: Paginacao<Destino>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao buscar destinos!'),
        });
    } else {
      this.destinoService
        .listar(this.page, this.size, this.colunaOrdenada, this.ordem)
        .subscribe({
          next: (resposta: Paginacao<Destino>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao listar destinos!'),
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
  ordenarPor(campo: keyof Destino) {
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
    this.modalRef = this.modalService.open(this.modalDestinoDetalhe);
  }

  // 📌 Modal de edição
  editarModal(destino: Destino) {
    this.registroSelecionado = { ...destino };
    this.modalRef = this.modalService.open(this.modalDestinoDetalhe);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar (cadastrar ou atualizar)
  salvarDestino(destino: Destino) {
    if (!destino.nome?.trim()) {
      this.toastService.showError('O campo nome é obrigatório.');
      return;
    }

    const isNovo = !destino.id || destino.id <= 0;

    if (isNovo) {
      this.destinoService.cadastrar(destino).subscribe({
        next: () => {
          this.toastService.showSuccess('Destino cadastrado com sucesso!');
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
      this.destinoService.atualizar(destino.id, destino).subscribe({
        next: () => {
          this.toastService.showSuccess('Destino atualizado com sucesso!');
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

  // 📌 Excluir destino
  excluir(destino: Destino) {
    this.registroSelecionado = destino;
    this.modalRef = this.modalService.open(this.modalConfirmacaoExclusao);
  }

  excluirConfirmado() {
    this.modalRef.close();
    this.destinoService.excluir(this.registroSelecionado.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Destino excluído com sucesso!');
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
