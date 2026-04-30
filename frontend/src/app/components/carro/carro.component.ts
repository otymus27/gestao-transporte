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
import { Carro } from '../../models/carro';
import { ErrorMessage, CarroService } from '../../services/carro.service';
import { TipoCarro } from './../../enums/tipo_carro.enum';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-carro',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './carro.component.html',
  styleUrl: './carro.component.scss',
})
export class CarroComponent {
  lista: Carro[] = [];
  registroSelecionado!: Carro;

  //lista de tipos atraves de enums
  tiposCarro = Object.values(TipoCarro);
  marcas = [
    'CITROEN',
    'FIAT',
    'FORD',
    'RENAULT',
    'VOLKSWAGEN',
    'CHEVROLET',
    'TOYOTA',
  ];

  modelosPorMarca: Record<string, string[]> = {
    CITROEN: ['MASTER', 'C3'],
    CHEVROLET: ['ONIX', 'PRISMA', 'S10', 'SPIN'],
    FIAT: ['DOBLO', 'PALIO WEEKEND', 'STRADA', 'TORO'],
    FORD: ['KA', 'FOCUS', 'FIESTA', 'FUSION'],
    RENAULT: ['CLIO', 'DUSTER', 'MASTER'],
    TOYOTA: ['COROLLA', 'YARIS', 'HILUX', 'ETIOS', 'SW4'],
    VOLKSWAGEN: ['GOL', 'POLO', 'VIRTUS', 'SAVEIRO'],
  };

  modelosFiltrados: string[] = [];

  onMarcaChange(marca: string) {
    this.modelosFiltrados = this.modelosPorMarca[marca] || [];
    this.registroSelecionado.modelo = ''; // limpa o modelo atual
  }

  // Paginação
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // Filtros
  filtroPlaca: string = '';
  filtroMarca: string = '';
  filtroModelo: string = '';

  // Ordenação
  colunaOrdenada: keyof Carro = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  // Injeções
  carroService = inject(CarroService);
  modalService = inject(MdbModalService);
  toastService = inject(ToastService);
  private authService = inject(AuthService);

  // Modais
  @ViewChild('modalCarroDetalhe') modalCarroDetalhe!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  canDelete(): boolean {
    return this.authService.getLoggedInRoles().includes('ADMIN');
  }

  @ViewChild('modalConfirmacaoExclusao')
  modalConfirmacaoExclusao!: TemplateRef<any>;

  constructor() {
    this.listar();
  }

  // 📌 Listar carros
  listar() {
    if (this.filtroPlaca || this.filtroMarca || this.filtroModelo) {
      this.carroService
        .filtrar(
          this.filtroPlaca,
          this.filtroMarca,
          this.filtroModelo,
          this.page,
          this.size,
          this.colunaOrdenada,
          this.ordem
        )
        .subscribe({
          next: (resposta: Paginacao<Carro>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao buscar carros!'),
        });
    } else {
      this.carroService
        .listar(this.page, this.size, this.colunaOrdenada, this.ordem)
        .subscribe({
          next: (resposta: Paginacao<Carro>) => {
            this.lista = resposta.content;
            this.page = resposta.number;
            this.totalPages = resposta.totalPages;
            this.totalElements = resposta.totalElements;
          },
          error: () => this.toastService.showError('Erro ao listar carros!'),
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
    this.filtroPlaca = '';
    this.filtroMarca = '';
    this.filtroModelo = '';
  }

  limparFiltros() {
    this.filtroPlaca = '';
    this.filtroMarca = '';
    this.filtroModelo = '';
    this.aplicarFiltros();
  }

  // 📌 Ordenação
  ordenarPor(campo: keyof Carro) {
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
      placa: '',
      marca: '',
      modelo: '',
      tipo: '',
    };
    this.modalRef = this.modalService.open(this.modalCarroDetalhe);
  }

  // 📌 Modal de edição
  editarModal(carro: Carro) {
    this.registroSelecionado = { ...carro };
    this.modalRef = this.modalService.open(this.modalCarroDetalhe);
  }

  cancelarModal() {
    this.modalRef.close();
  }

  // 📌 Salvar (cadastrar ou atualizar)
  salvarCarro(carro: Carro) {
    if (
      !carro.placa?.trim() ||
      !carro.marca?.trim() ||
      !carro.modelo?.trim() ||
      !carro.tipo?.trim()
    ) {
      this.toastService.showError('Todos os campos são obrigatórios.');
      return;
    }

    const isNovo = !carro.id || carro.id <= 0;

    if (isNovo) {
      this.carroService.cadastrar(carro).subscribe({
        next: () => {
          this.toastService.showSuccess('Carro cadastrado com sucesso!');
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
      this.carroService.atualizar(carro.id, carro).subscribe({
        next: () => {
          this.toastService.showSuccess('Carro atualizado com sucesso!');
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

  // 📌 Excluir carro
  excluir(carro: Carro) {
    this.registroSelecionado = carro;
    this.modalRef = this.modalService.open(this.modalConfirmacaoExclusao);
  }

  excluirConfirmado() {
    this.modalRef.close();
    this.carroService.excluir(this.registroSelecionado.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Carro excluído com sucesso!');
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
