// src/app/components/motorista/motorista.component.ts
import { CommonModule } from '@angular/common';
import { Component, inject, TemplateRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { NgxMaskDirective } from 'ngx-mask';
import {
  MdbModalModule,
  MdbModalRef,
  MdbModalService,
} from 'mdb-angular-ui-kit/modal';

import { ToastService } from '../../services/toast.service';
import { Paginacao } from '../../models/paginacao';
import { Motorista } from '../../models/motorista';
import {
  ErrorMessage,
  MotoristaService,
} from '../../services/motorista.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-motorista',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MdbFormsModule,
    MdbModalModule,
    NgxMaskDirective,
  ],
  templateUrl: './motorista.component.html',
  styleUrl: './motorista.component.scss',
})
export class MotoristaComponent {
  lista: Motorista[] = [];
  registroSelecionado!: Motorista;

  // Regex para formato do telefone
  telefoneRegex: RegExp = /^\([0-9]{2}\) [0-9]{4,5}-[0-9]{4}$/;

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtros
  filtroNome: string = '';
  filtroMatricula: string = '';

  // Ordenação
  colunaOrdenada: keyof Motorista = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  // Injeções
  motoristaService = inject(MotoristaService);
  modalService = inject(MdbModalService);
  toastService = inject(ToastService);
  private authService = inject(AuthService);

  // Modais
  @ViewChild('modalMotoristaDetalhe') modalMotoristaDetalhe!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  canDelete(): boolean {
    return this.authService.getLoggedInRoles().includes('ADMIN');
  }

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  constructor() {
    this.listar();
  }

  // 📌 Listar motoristas
  listar() {
    if (this.filtroNome?.trim() || this.filtroMatricula?.trim()) {
      this.motoristaService
        .filtrar(
          this.filtroNome,
          this.filtroMatricula,
          this.page,
          this.size,
          this.colunaOrdenada,
          this.ordem
        )
        .subscribe({
          next: (resposta: Paginacao<Motorista>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () =>
            this.toastService.showError('Erro ao buscar motoristas!'),
        });
    } else {
      this.motoristaService
        .listar(this.page, this.size, this.colunaOrdenada, this.ordem)
        .subscribe({
          next: (resposta: Paginacao<Motorista>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () =>
            this.toastService.showError('Erro ao listar motoristas!'),
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
    this.filtroMatricula = '';
  }

  limparFiltros() {
    this.filtroNome = '';
    this.filtroMatricula = '';
    this.aplicarFiltros();
  }

  // 📌 Ordenação
  ordenarPor(campo: keyof Motorista) {
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
    this.registroSelecionado = {
      id: 0,
      nome: '',
      matricula: '',
      telefone: '',
      ativo: true,
    };
    this.modalRef = this.modalService.open(this.modalMotoristaDetalhe);
  }

  // 📌 Modal de edição
  editarModal(motorista: Motorista) {
    this.registroSelecionado = { ...motorista };
    this.modalRef = this.modalService.open(this.modalMotoristaDetalhe);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar (cadastrar ou atualizar)
  salvarMotorista(motorista: Motorista) {
    if (!motorista.nome?.trim()) {
      this.toastService.showError('O campo nome é obrigatório.');
      return;
    }
    if (!motorista.matricula?.trim()) {
      this.toastService.showError('O campo matrícula é obrigatório.');
      return;
    }

    if (!motorista.telefone?.trim()) {
      this.toastService.showError('O campo telefone é obrigatório.');
      return;
    }

    if (!this.telefoneRegex.test(motorista.telefone)) {
      this.toastService.showError(
        'Telefone inválido. Use o formato (99) 99999-9999.'
      );
      return;
    }

    const isNovo = !motorista.id || motorista.id <= 0;

    if (isNovo) {
      this.motoristaService.cadastrar(motorista).subscribe({
        next: () => {
          this.toastService.showSuccess('Motorista cadastrado com sucesso!');
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
      this.motoristaService.atualizar(motorista.id, motorista).subscribe({
        next: () => {
          this.toastService.showSuccess('Motorista atualizado com sucesso!');
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

  // 📌 Excluir motorista
  excluir(motorista: Motorista) {
    this.registroSelecionado = motorista;
    this.modalRef = this.modalService.open(this.modalConfirmacaoExclusao);
  }

  excluirConfirmado() {
    this.modalRef.close();
    this.motoristaService.excluir(this.registroSelecionado.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Motorista excluído com sucesso!');
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
