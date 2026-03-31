import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { MdbModalModule } from 'mdb-angular-ui-kit/modal';

import { ToastService } from '../../../services/toast.service';
import { Paginacao } from '../../../models/paginacao';
import { Solicitacao, StatusSolicitacao } from '../../../models/solicitacao';
import { SolicitacaoService } from '../../../services/solicitacao.service';

@Component({
  selector: 'app-solicitacao',
  standalone: true,
  imports: [CommonModule, FormsModule, MdbFormsModule, MdbModalModule],
  templateUrl: './solicitacao.component.html',
  styleUrl: './solicitacao.component.scss',
})
export class SolicitacaoComponent implements OnInit {
  lista: Solicitacao[] = [];
  statusOptions: StatusSolicitacao[] = [
    'PENDENTE',
    'EM_ANDAMENTO',
    'CONCLUIDO',
    'CANCELADO',
  ];

  // Paginação
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  // Filtros
  filtroId: number | null = null;
  filtroStatus = '';
  mostrarSomentePendentes = false;

  // Ordenação
  colunaOrdenada: keyof Solicitacao = 'id';
  ordem: 'asc' | 'desc' = 'asc';

  solicitacaoService = inject(SolicitacaoService);
  toastService = inject(ToastService);

  ngOnInit(): void {
    this.listar();
  }

  // =========================================================
  // FILTROS / LISTAGEM
  // =========================================================
  usarFiltro(chave: 'id' | 'status') {
    if (chave !== 'id') this.filtroId = null;
    if (chave !== 'status') this.filtroStatus = '';
    if (chave !== 'status') this.mostrarSomentePendentes = false;
  }

  alternarFiltroPendentes() {
    this.mostrarSomentePendentes = !this.mostrarSomentePendentes;
    this.filtroStatus = this.mostrarSomentePendentes ? 'PENDENTE' : '';
    this.page = 0;
    this.listar();
  }

  listar() {
    this.page = Math.max(0, this.page);
    let obs$;

    if (this.filtroId != null) {
      obs$ = this.solicitacaoService.filtrarGenerico(
        { id: this.filtroId },
        this.page,
        this.size
      );
    } else if (this.filtroStatus.trim()) {
      obs$ = this.solicitacaoService.filtrarGenerico(
        { status: this.filtroStatus.trim() },
        this.page,
        this.size
      );
    } else {
      obs$ = this.solicitacaoService.listar(
        this.page,
        this.size,
        this.colunaOrdenada,
        this.ordem
      );
    }

    obs$.subscribe({
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

  aplicarFiltros() {
    this.page = 0;
    this.listar();
  }

  limparFiltros() {
    this.filtroId = null;
    this.filtroStatus = '';
    this.mostrarSomentePendentes = false;
    this.page = 0;
    this.listar();
  }

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

  ordenarPor(campo: keyof Solicitacao) {
    this.ordem =
      this.colunaOrdenada === campo && this.ordem === 'asc' ? 'desc' : 'asc';
    this.colunaOrdenada = campo;
    this.listar();
  }

  calcularKmTotal(sol: Solicitacao): number | string {
    if (!sol.kmInicial || !sol.kmFinal) return '-';
    return sol.kmFinal - sol.kmInicial;
  }

  get paginasVisiveis(): (number | null)[] {
    const total = this.totalPages;
    const atual = this.page;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);

    const set = new Set<number>();
    set.add(0);
    set.add(total - 1);
    for (
      let i = Math.max(0, atual - 2);
      i <= Math.min(total - 1, atual + 2);
      i++
    ) {
      set.add(i);
    }
    const sorted = Array.from(set).sort((a, b) => a - b);
    const result: (number | null)[] = [];
    for (let i = 0; i < sorted.length; i++) {
      if (i > 0 && sorted[i] - sorted[i - 1] > 1) result.push(null);
      result.push(sorted[i]);
    }
    return result;
  }
}
