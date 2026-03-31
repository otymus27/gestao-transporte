// src/app/components/ficha/ficha.component.ts
import { Component, inject, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { MdbModalModule, MdbModalRef, MdbModalService } from 'mdb-angular-ui-kit/modal';

import { ToastService } from '../../services/toast.service';
import { MotoristaService } from '../../services/motorista.service';
import { SetorService } from '../../services/setor.service';
import { DestinoService } from '../../services/destino.service';
import { Paginacao } from '../../models/paginacao';
import { Motorista } from '../../models/motorista';
import { Setor } from '../../models/setor';
import { Destino } from '../../models/destino';
import {
  ErrorMessage,
  FichaSolicitacaoService,
} from '../../services/ficha-solicitacao.service';
import {
  FichaSolicitacaoRequest,
  FichaSolicitacaoResponse,
  SolicitacaoItem,
  novaFicha,
  novaSolicitacaoItem,
} from '../../models/ficha-solicitacao';

@Component({
  selector: 'app-ficha',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './ficha.component.html',
  styleUrl: './ficha.component.scss',
})
export class FichaComponent implements OnInit {

  // =========================================================
  // LISTA / PAGINAÇÃO
  // =========================================================
  lista: FichaSolicitacaoResponse[] = [];
  page = 0;
  size = 5;
  totalPages = 0;
  totalElements = 0;

  // =========================================================
  // FILTROS
  // =========================================================
  filtroPlaca = '';
  filtroInicio = '';
  filtroFim = '';

  // =========================================================
  // FICHA SENDO EDITADA / CRIADA
  // =========================================================
  ficha: FichaSolicitacaoRequest = novaFicha();
  fichaIdEditando: number | null = null;     // null = novo, number = edição

  // Solicitação temporária (formulário do item antes de adicionar à lista)
  itemAtual: SolicitacaoItem = novaSolicitacaoItem();

  // Nomes para exibição nos autocompletes
  itemMotorista = '';
  itemSetor = '';
  itemDestino = '';

  // =========================================================
  // DETALHE
  // =========================================================
  fichaDetalhe: FichaSolicitacaoResponse | null = null;

  // =========================================================
  // LISTAS DOS COMBOS
  // =========================================================
  motoristas: Motorista[] = [];
  setores: Setor[] = [];
  destinos: Destino[] = [];

  // =========================================================
  // MODAIS
  // =========================================================
  @ViewChild('modalFicha')       modalFicha!: TemplateRef<any>;
  @ViewChild('modalDetalhe')     modalDetalhe!: TemplateRef<any>;
  @ViewChild('modalConfirmar')   modalConfirmar!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;
  fichaParaExcluir: FichaSolicitacaoResponse | null = null;

  // =========================================================
  // INJEÇÕES
  // =========================================================
  private fichaService    = inject(FichaSolicitacaoService);
  private motoristaService = inject(MotoristaService);
  private setorService    = inject(SetorService);
  private destinoService  = inject(DestinoService);
  private modalService    = inject(MdbModalService);
  toastService            = inject(ToastService);

  // =========================================================
  // LIFECYCLE
  // =========================================================
  ngOnInit(): void {
    this.listar();
  }

  // =========================================================
  // LISTAGEM
  // =========================================================
  listar() {
    this.page = Math.max(0, this.page);
    const temFiltro = this.filtroPlaca || this.filtroInicio || this.filtroFim;

    const obs = temFiltro
      ? this.fichaService.filtrar(
          { placa: this.filtroPlaca, inicio: this.filtroInicio, fim: this.filtroFim },
          this.page, this.size
        )
      : this.fichaService.listar(this.page, this.size);

    obs.subscribe({
      next: (res: Paginacao<FichaSolicitacaoResponse>) => {
        this.lista       = res.content;
        this.page        = res.number;
        this.totalPages  = res.totalPages;
        this.totalElements = res.totalElements;
      },
      error: () => this.toastService.showError('Erro ao carregar fichas!'),
    });
  }

  aplicarFiltros() { this.page = 0; this.listar(); }

  limparFiltros() {
    this.filtroPlaca = '';
    this.filtroInicio = '';
    this.filtroFim = '';
    this.page = 0;
    this.listar();
  }

  irParaPagina(p: number)  { this.page = p; this.listar(); }
  proximaPagina()           { if (this.page < this.totalPages - 1) { this.page++; this.listar(); } }
  paginaAnterior()          { if (this.page > 0) { this.page--; this.listar(); } }

  // =========================================================
  // MODAL CRIAR / EDITAR FICHA
  // =========================================================
  abrirModalNova() {
    this.fichaIdEditando = null;
    this.ficha = novaFicha();
    this.itemAtual = novaSolicitacaoItem();
    this.limparCamposItem();
    this.modalRef = this.modalService.open(this.modalFicha, { modalClass: 'modal-xl' });
  }

  abrirModalEditar(f: FichaSolicitacaoResponse) {
    this.fichaIdEditando = f.id;
    this.ficha = {
      dataViagem:   f.dataViagem,
      placaVeiculo: f.placaVeiculo,
      solicitacoes: f.solicitacoes.map(s => ({ ...s })),
    };
    this.itemAtual = novaSolicitacaoItem();
    this.limparCamposItem();
    this.modalRef = this.modalService.open(this.modalFicha, { modalClass: 'modal-xl' });
  }

  cancelarModal() { this.modalRef.close(); }

  // =========================================================
  // GERENCIAR SOLICITAÇÕES DENTRO DA FICHA (LISTA LOCAL)
  // =========================================================

  // Autocomplete motorista
  buscarMotoristas(query: string) {
    if (!query.trim()) { this.motoristas = []; return; }
    this.motoristaService.buscarParaCombo(query).subscribe({
      next: (lista) => (this.motoristas = lista),
      error: () => this.toastService.showError('Erro ao buscar motoristas!'),
    });
  }
  selecionarMotorista(m: Motorista) {
    this.itemAtual.motoristaId = m.id!;
    this.itemMotorista = m.nome;
    this.motoristas = [];
  }

  // Autocomplete setor
  buscarSetores(query: string) {
    if (!query.trim()) { this.setores = []; return; }
    this.setorService.filtrarPorNome(query).subscribe({
      next: (res) => (this.setores = res.content),
      error: () => this.toastService.showError('Erro ao buscar setores!'),
    });
  }
  selecionarSetor(s: Setor) {
    this.itemAtual.setorId = s.id!;
    this.itemSetor = s.nome;
    this.setores = [];
  }

  // Autocomplete destino
  buscarDestinos(query: string) {
    if (!query.trim()) { this.destinos = []; return; }
    this.destinoService.filtrarPorNome(query).subscribe({
      next: (res) => (this.destinos = res.content),
      error: () => this.toastService.showError('Erro ao buscar destinos!'),
    });
  }
  selecionarDestino(d: Destino) {
    this.itemAtual.destinoId = d.id!;
    this.itemDestino = d.nome;
    this.destinos = [];
  }

  /** Adiciona o item atual à lista local (SEM chamar a API) */
  adicionarItem() {
    if (!this.itemAtual.motoristaId || !this.itemAtual.setorId || !this.itemAtual.destinoId) {
      this.toastService.showError('Motorista, Setor e Destino são obrigatórios.');
      return;
    }

    this.ficha.solicitacoes.push({
      ...this.itemAtual,
      motoristaNome: this.itemMotorista,
      setorNome:     this.itemSetor,
      destinoNome:   this.itemDestino,
    });

    // Limpa o formulário do item para o próximo
    this.itemAtual = novaSolicitacaoItem();
    this.limparCamposItem();
  }

  removerItem(index: number) {
    this.ficha.solicitacoes.splice(index, 1);
  }

  private limparCamposItem() {
    this.itemMotorista = '';
    this.itemSetor     = '';
    this.itemDestino   = '';
    this.motoristas    = [];
    this.setores       = [];
    this.destinos      = [];
  }

  // =========================================================
  // SALVAR FICHA (cria ou atualiza tudo de uma vez)
  // =========================================================
  salvarFicha() {
    if (!this.ficha.placaVeiculo?.trim()) {
      this.toastService.showError('Placa do veículo é obrigatória.');
      return;
    }
    if (!this.ficha.dataViagem) {
      this.toastService.showError('Data da viagem é obrigatória.');
      return;
    }
    if (this.ficha.solicitacoes.length === 0) {
      this.toastService.showError('Adicione ao menos uma solicitação à ficha.');
      return;
    }

    const obs = this.fichaIdEditando
      ? this.fichaService.atualizar(this.fichaIdEditando, this.ficha)
      : this.fichaService.salvar(this.ficha);

    obs.subscribe({
      next: () => {
        const msg = this.fichaIdEditando ? 'Ficha atualizada' : 'Ficha salva';
        this.toastService.showSuccess(`${msg} com sucesso!`);
        this.listar();
        this.modalRef.close();
      },
      error: (err: ErrorMessage) =>
        this.toastService.showError(`Erro (${err.status}): ${err.message}`),
    });
  }

  // =========================================================
  // DETALHE
  // =========================================================
  verDetalhe(f: FichaSolicitacaoResponse) {
    this.fichaDetalhe = f;
    this.modalRef = this.modalService.open(this.modalDetalhe, { modalClass: 'modal-lg' });
  }

  // =========================================================
  // EXCLUIR
  // =========================================================
  confirmarExclusao(f: FichaSolicitacaoResponse) {
    this.fichaParaExcluir = f;
    this.modalRef = this.modalService.open(this.modalConfirmar);
  }

  excluirConfirmado() {
    this.modalRef.close();
    this.fichaService.excluir(this.fichaParaExcluir!.id).subscribe({
      next: () => {
        this.toastService.showSuccess('Ficha excluída com sucesso!');
        this.listar();
      },
      error: (err: ErrorMessage) =>
        this.toastService.showError(`Erro (${err.status}): ${err.message}`),
    });
  }

  // =========================================================
  // UTILITÁRIOS
  // =========================================================
  calcularKm(item: SolicitacaoItem): string {
    if (item.kmInicial == null || item.kmFinal == null) return '-';
    return String(item.kmFinal - item.kmInicial) + ' km';
  }

  get paginasArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}