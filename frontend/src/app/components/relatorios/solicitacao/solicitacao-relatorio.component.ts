import { Component, ElementRef, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { RelatorioService } from '../../../services/relatorios.service';

@Component({
  selector: 'app-solicitacoes-relatorio',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitacao-relatorio.component.html',
  styleUrls: ['./solicitacao-relatorio.component.scss'],
})
export class SolicitacoesRelatorioComponent implements OnInit {
  constructor(
    private relatorioService: RelatorioService,
    private eRef: ElementRef
  ) {}

  filtros = {
    id: '',
    status: '',
    motoristaId: '',
    carroId: '',
    setorId: '',
    username: '',
    destinoId: '',
  };

  resultados: any[] = [];
  carregando = false;

  statusOptions = ['PENDENTE', 'EMPRESTADO', 'DEVOLVIDO', 'CANCELADO'];

  // 🔹 Listas
  motoristas: any[] = [];
  carros: any[] = [];
  setores: any[] = [];
  destinos: any[] = [];

  // 🔹 Campos temporários (texto digitado)
  motoristaNome = '';
  carroTexto = '';
  setorTexto = '';
  destinoTexto = '';

  // 🔹 Observables para busca dinâmica
  motoristaSearch$ = new Subject<string>();
  carroSearch$ = new Subject<string>();
  setorSearch$ = new Subject<string>();
  destinoSearch$ = new Subject<string>();

  ngOnInit(): void {
    // Motoristas
    this.motoristaSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((term) =>
          this.relatorioService.listarMotoristas({ nome: term })
        )
      )
      .subscribe((data) => (this.motoristas = data.content || data));

    // Carros
    this.carroSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((term) => {
          // Detecta automaticamente se o usuário digitou uma placa
          const filtro: any = {};
          const termo = term?.trim().toUpperCase();

          if (!termo) return [];

          // 🔹 Placa normalmente tem formato com letras e números (ex: ABC1D23)
          const padraoPlaca = /^[A-Z]{3}\d[A-Z0-9]\d{2}$/;
          const padraoAntigo = /^[A-Z]{3}-?\d{4}$/;

          if (padraoPlaca.test(termo) || padraoAntigo.test(termo)) {
            filtro.placa = termo.replace('-', '');
          } else {
            filtro.marca = termo;
            filtro.modelo = termo;
          }

          return this.relatorioService.listarCarros(filtro);
        })
      )
      .subscribe((data) => (this.carros = data.content || data));

    // Setores
    this.setorSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((term) => this.relatorioService.listarSetores({ nome: term }))
      )
      .subscribe((data) => (this.setores = data.content || data));

    // Destinos
    this.destinoSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((term) =>
          this.relatorioService.listarDestinos({ nome: term })
        )
      )
      .subscribe((data) => (this.destinos = data.content || data));
  }

  // 🔹 Seleções
  selecionarMotorista(m: any) {
    this.filtros.motoristaId = m.id;
    this.motoristaNome = m.nome;
    this.motoristas = [];
  }

  selecionarCarro(c: any) {
    this.filtros.carroId = c.id;
    this.carroTexto = `${c.placa} - ${c.marca}`;
    this.carros = [];
  }

  selecionarSetor(s: any) {
    this.filtros.setorId = s.id;
    this.setorTexto = s.nome;
    this.setores = [];
  }

  selecionarDestino(d: any) {
    this.filtros.destinoId = d.id;
    this.destinoTexto = d.nome;
    this.destinos = [];
  }

  // 🔹 Fechar dropdowns ao clicar fora
  @HostListener('document:click', ['$event'])
  clickFora(event: Event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.motoristas = [];
      this.carros = [];
      this.setores = [];
      this.destinos = [];
    }
  }

  // 🔹 Reativar busca ao limpar campo
  limparCampo(tipo: string) {
    if (tipo === 'motorista') {
      this.motoristaNome = '';
      this.filtros.motoristaId = '';
    }
    if (tipo === 'carro') {
      this.carroTexto = '';
      this.filtros.carroId = '';
    }
    if (tipo === 'setor') {
      this.setorTexto = '';
      this.filtros.setorId = '';
    }
    if (tipo === 'destino') {
      this.destinoTexto = '';
      this.filtros.destinoId = '';
    }
  }

  // 🔹 Buscar solicitações
  consultar(): void {
    this.carregando = true;
    this.relatorioService.listarSolicitacoes(this.filtros).subscribe({
      next: (data) => {
        this.resultados = data.content || data;
        this.carregando = false;
      },
      error: () => (this.carregando = false),
    });
  }

  limpar(): void {
    this.filtros = {
      id: '',
      status: '',
      motoristaId: '',
      carroId: '',
      setorId: '',
      username: '',
      destinoId: '',
    };
    this.motoristaNome = '';
    this.carroTexto = '';
    this.setorTexto = '';
    this.destinoTexto = '';
    this.resultados = [];
  }

  exportar(tipo: string): void {
    const filtroGlobal = Object.values(this.filtros).filter(Boolean).join(' ');
    this.relatorioService
      .exportarSolicitacoes(tipo, filtroGlobal)
      .subscribe((blob) => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `relatorio_solicitacoes.${tipo}`;
        link.click();
      });
  }
}
