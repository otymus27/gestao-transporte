import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../services/dashboard.service';
import { SolicitacaoService } from '../../services/solicitacao.service';
import { MotoristaService } from '../../services/motorista.service';
import { CarroService } from '../../services/carro.service';
import { SetorService } from '../../services/setor.service';
import { DestinoService } from '../../services/destino.service';
import {
  RelatorioSolicitacaoService,
  TipoRelatorioSolicitacao,
} from '../../services/relatorios/relatorio-solicitacao.service';
import { RelatorioMotoristaService } from '../../services/relatorios/relatorio-motorista.service';
import { RelatorioCarroService } from '../../services/relatorios/relatorio-carro.service';
import { RelatorioSetorService } from '../../services/relatorios/relatorio-setor.service';
import { RelatorioDestinoService } from '../../services/relatorios/relatorio-destino.service';
import { RelatorioProducaoService } from '../../services/relatorios/relatorio-producao.service';

export type ModuloRelatorio =
  | 'solicitacoes'
  | 'motoristas'
  | 'carros'
  | 'setores'
  | 'destinos'
  | 'producao';

type ModuloCard = {
  key: ModuloRelatorio;
  label: string;
  icon: string;
  cor: string;
};

@Component({
  selector: 'app-relatorio-central',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: '../relatorios/central.component.html',
  styleUrl: '../relatorios/centra.component.scss',
})
export class RelatorioCentralComponent implements OnInit {
  moduloAtivo: ModuloRelatorio = 'solicitacoes';

  totalSolicitacoes = 0;
  totalMotoristas = 0;
  totalCarros = 0;
  totalSetores = 0;
  totalDestinos = 0;

  resultados: any[] = [];
  carregando = false;
  consultado = false;
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  modulos: ModuloCard[] = [
    {
      key: 'solicitacoes',
      label: 'Solicitações',
      icon: 'bi-journal-text',
      cor: 'blue',
    },
    {
      key: 'motoristas',
      label: 'Motoristas',
      icon: 'bi-person-vcard',
      cor: 'green',
    },
    {
      key: 'carros',
      label: 'Carros',
      icon: 'bi-car-front-fill',
      cor: 'purple',
    },
    { key: 'setores', label: 'Setores', icon: 'bi-building', cor: 'orange' },
    {
      key: 'destinos',
      label: 'Destinos',
      icon: 'bi-geo-alt-fill',
      cor: 'teal',
    },
    {
      key: 'producao',
      label: 'Produção por Usuário',
      icon: 'bi-person-check-fill',
      cor: 'indigo',
    },
  ];

  filtroSol = {
    tipo: 'SIMPLES' as TipoRelatorioSolicitacao,
    filtro: '',
    dataInicio: '',
    dataFim: '',
  };

  tiposSolicitacao = [
    { value: 'SIMPLES', label: 'Sem agrupamento' },
    { value: 'POR_SETOR', label: 'Por Setor' },
    { value: 'POR_MOTORISTA', label: 'Por Motorista' },
    { value: 'POR_CARRO', label: 'Por Veículo' },
    { value: 'POR_DESTINO', label: 'Por Destino' },
    { value: 'POR_USUARIO', label: 'Por Usuário' },
  ];

  filtroMot = { nome: '', matricula: '' };

  filtroCar = { placa: '', marca: '', modelo: '', tipo: '' };
  marcasCarro = [
    'CITROEN',
    'FIAT',
    'FORD',
    'RENAULT',
    'VOLKSWAGEN',
    'CHEVROLET',
    'TOYOTA',
  ];
  tiposCarro = [
    'PASSEIO',
    'UTILITARIO',
    'VAN',
    'CAMINHAO',
    'ONIBUS',
    'AMBULANCIA',
    'AUTOMOVEL',
  ];

  filtroSet = { nome: '' };
  filtroDest = { nome: '' };

  filtroProd = { nomeUsuario: '', dataInicio: '', dataFim: '' };

  colunas: { key: string; label: string; center?: boolean }[] = [];

  constructor(
    private dashboardService: DashboardService,
    private solicitacaoService: SolicitacaoService,
    private motoristaService: MotoristaService,
    private carroService: CarroService,
    private setorService: SetorService,
    private destinoService: DestinoService,
    private relSolService: RelatorioSolicitacaoService,
    private relMotService: RelatorioMotoristaService,
    private relCarService: RelatorioCarroService,
    private relSetService: RelatorioSetorService,
    private relDestService: RelatorioDestinoService,
    private relProdService: RelatorioProducaoService
  ) {}

  ngOnInit(): void {
    this.carregarResumo();
    this.selecionarModulo('solicitacoes');
  }

  getSubtituloModulo(modulo: ModuloRelatorio): string {
    switch (modulo) {
      case 'solicitacoes':
        return `${this.totalSolicitacoes} registros`;
      case 'motoristas':
        return `${this.totalMotoristas} cadastrados`;
      case 'carros':
        return `${this.totalCarros} cadastrados`;
      case 'setores':
        return `${this.totalSetores} cadastrados`;
      case 'destinos':
        return `${this.totalDestinos} cadastrados`;
      case 'producao':
        return 'fichas e solicitações';
    }
  }

  carregarResumo(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (d) => {
        this.totalSolicitacoes = d.totalSolicitacoes;
        this.totalMotoristas = d.totalMotoristas;
        this.totalCarros = d.totalCarros;
        this.totalSetores = d.totalSetores;
      },
    });

    this.destinoService.listar(0, 1).subscribe({
      next: (r) => (this.totalDestinos = r.totalElements),
    });
  }

  selecionarModulo(modulo: ModuloRelatorio): void {
    this.moduloAtivo = modulo;
    this.resultados = [];
    this.consultado = false;
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
    this.definirColunas(modulo);
  }

  definirColunas(modulo: ModuloRelatorio): void {
    const map: Record<
      ModuloRelatorio,
      { key: string; label: string; center?: boolean }[]
    > = {
      solicitacoes: [
        { key: 'id', label: 'ID', center: true },
        { key: 'dataSolicitacao', label: 'Data' },
        { key: 'status', label: 'Status', center: true },
        { key: 'carro', label: 'Veículo' },
        { key: 'motorista', label: 'Motorista' },
        { key: 'setor', label: 'Setor' },
        { key: 'destino', label: 'Destino' },
        { key: 'kmInicial', label: 'KM Ini', center: true },
        { key: 'kmFinal', label: 'KM Fin', center: true },
        { key: 'kmTotal', label: 'Total KM', center: true },
        { key: 'horaSaida', label: 'Saída', center: true },
        { key: 'horaChegada', label: 'Chegada', center: true },
      ],
      motoristas: [
        { key: 'id', label: 'ID', center: true },
        { key: 'matricula', label: 'Matrícula' },
        { key: 'nome', label: 'Nome' },
        { key: 'cnh', label: 'CNH' },
        { key: 'ativo', label: 'Ativo', center: true },
      ],
      carros: [
        { key: 'id', label: 'ID', center: true },
        { key: 'placa', label: 'Placa' },
        { key: 'marca', label: 'Marca' },
        { key: 'modelo', label: 'Modelo' },
        { key: 'tipo', label: 'Tipo', center: true },
      ],
      setores: [
        { key: 'id', label: 'ID', center: true },
        { key: 'nome', label: 'Nome' },
      ],
      destinos: [
        { key: 'id', label: 'ID', center: true },
        { key: 'nome', label: 'Nome' },
      ],
      producao: [
        { key: 'usuarioNome', label: 'Usuário' },
        { key: 'username', label: 'Login' },
        { key: 'quantidadeFichas', label: 'Qtd. Fichas', center: true },
        {
          key: 'quantidadeSolicitacoes',
          label: 'Qtd. Solicitações',
          center: true,
        },
        { key: 'totalGeral', label: 'Total', center: true },
      ],
    };

    this.colunas = map[modulo];
  }

  consultar(page = 0): void {
    this.carregando = true;
    this.consultado = true;
    this.page = page;

    const next = (resp: any) => {
      this.resultados = resp.content ?? [];
      this.totalPages = resp.totalPages ?? 0;
      this.totalElements = resp.totalElements ?? 0;
      this.page = resp.number ?? 0;
      this.size = resp.size ?? this.size;
      this.carregando = false;
    };

    const error = () => {
      this.resultados = [];
      this.carregando = false;
    };

    switch (this.moduloAtivo) {
      case 'solicitacoes':
        this.solicitacaoService
          .consultarParaRelatorio(
            {
              filtro: this.filtroSol.filtro?.trim() || undefined,
              dataInicio: this.filtroSol.dataInicio || undefined,
              dataFim: this.filtroSol.dataFim || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;

      case 'motoristas':
        this.motoristaService
          .consultarParaRelatorio(
            {
              nome: this.filtroMot.nome?.trim() || undefined,
              matricula: this.filtroMot.matricula?.trim() || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;

      case 'carros':
        this.carroService
          .consultarParaRelatorio(
            {
              placa: this.filtroCar.placa?.trim() || undefined,
              marca: this.filtroCar.marca?.trim() || undefined,
              modelo: this.filtroCar.modelo?.trim() || undefined,
              tipo: this.filtroCar.tipo || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;

      case 'setores':
        this.setorService
          .consultarParaRelatorio(
            {
              nome: this.filtroSet.nome?.trim() || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;

      case 'destinos':
        this.destinoService
          .consultarParaRelatorio(
            {
              nome: this.filtroDest.nome?.trim() || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;

      case 'producao':
        this.relProdService
          .consultar(
            {
              nomeUsuario: this.filtroProd.nomeUsuario?.trim() || undefined,
              dataInicio: this.filtroProd.dataInicio || undefined,
              dataFim: this.filtroProd.dataFim || undefined,
            },
            this.page,
            this.size
          )
          .subscribe({ next, error });
        break;
    }
  }

  limpar(): void {
    this.filtroSol = {
      tipo: 'SIMPLES',
      filtro: '',
      dataInicio: '',
      dataFim: '',
    };
    this.filtroMot = { nome: '', matricula: '' };
    this.filtroCar = { placa: '', marca: '', modelo: '', tipo: '' };
    this.filtroSet = { nome: '' };
    this.filtroDest = { nome: '' };
    this.filtroProd = { nomeUsuario: '', dataInicio: '', dataFim: '' };
    this.resultados = [];
    this.consultado = false;
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
  }

  exportar(tipo: 'pdf' | 'excel'): void {
    const ext = tipo === 'pdf' ? 'pdf' : 'xlsx';
    const contentType =
      tipo === 'pdf'
        ? 'application/pdf'
        : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    const hoje = new Date().toISOString().slice(0, 10);

    let obs$: any;

    switch (this.moduloAtivo) {
      case 'solicitacoes':
        obs$ =
          tipo === 'pdf'
            ? this.relSolService.exportarPdf({
                tipo: this.filtroSol.tipo,
                filtro: this.filtroSol.filtro?.trim() || undefined,
                dataInicio: this.filtroSol.dataInicio || undefined,
                dataFim: this.filtroSol.dataFim || undefined,
              })
            : this.relSolService.exportarExcel({
                tipo: this.filtroSol.tipo,
                filtro: this.filtroSol.filtro?.trim() || undefined,
                dataInicio: this.filtroSol.dataInicio || undefined,
                dataFim: this.filtroSol.dataFim || undefined,
              });
        break;

      case 'motoristas':
        obs$ =
          tipo === 'pdf'
            ? this.relMotService.exportarPdf({
                nome: this.filtroMot.nome?.trim() || undefined,
                matricula: this.filtroMot.matricula?.trim() || undefined,
              })
            : this.relMotService.exportarExcel({
                nome: this.filtroMot.nome?.trim() || undefined,
                matricula: this.filtroMot.matricula?.trim() || undefined,
              });
        break;

      case 'carros':
        obs$ =
          tipo === 'pdf'
            ? this.relCarService.exportarPdf({
                placa: this.filtroCar.placa?.trim() || undefined,
                marca: this.filtroCar.marca?.trim() || undefined,
                modelo: this.filtroCar.modelo?.trim() || undefined,
                tipo: this.filtroCar.tipo || undefined,
              })
            : this.relCarService.exportarExcel({
                placa: this.filtroCar.placa?.trim() || undefined,
                marca: this.filtroCar.marca?.trim() || undefined,
                modelo: this.filtroCar.modelo?.trim() || undefined,
                tipo: this.filtroCar.tipo || undefined,
              });
        break;

      case 'setores':
        obs$ =
          tipo === 'pdf'
            ? this.relSetService.exportarPdf({
                nome: this.filtroSet.nome?.trim() || undefined,
              })
            : this.relSetService.exportarExcel({
                nome: this.filtroSet.nome?.trim() || undefined,
              });
        break;

      case 'destinos':
        obs$ =
          tipo === 'pdf'
            ? this.relDestService.exportarPdf({
                nome: this.filtroDest.nome?.trim() || undefined,
              })
            : this.relDestService.exportarExcel({
                nome: this.filtroDest.nome?.trim() || undefined,
              });
        break;

      case 'producao':
        obs$ =
          tipo === 'pdf'
            ? this.relProdService.exportarPdf({
                nomeUsuario: this.filtroProd.nomeUsuario?.trim() || undefined,
                dataInicio: this.filtroProd.dataInicio || undefined,
                dataFim: this.filtroProd.dataFim || undefined,
              })
            : this.relProdService.exportarExcel({
                nomeUsuario: this.filtroProd.nomeUsuario?.trim() || undefined,
                dataInicio: this.filtroProd.dataInicio || undefined,
                dataFim: this.filtroProd.dataFim || undefined,
              });
        break;
    }

    obs$?.subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(
          new Blob([blob], { type: contentType })
        );
        const a = document.createElement('a');
        a.href = url;
        a.download = `rel_${this.moduloAtivo}_${hoje}.${ext}`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (e: any) => console.error('Erro ao exportar', e),
    });
  }

  exportarCsv(): void {
    if (!this.resultados.length) return;

    const header = this.colunas.map((c) => c.label);
    const rows = this.resultados.map((r) =>
      this.colunas.map((c) => r[c.key] ?? '')
    );
    const csv = [header, ...rows]
      .map((row) =>
        row.map((v) => `"${String(v).replaceAll('"', '""')}"`).join(';')
      )
      .join('\n');

    const url = window.URL.createObjectURL(
      new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    );
    const a = document.createElement('a');
    a.href = url;
    a.download = `rel_${this.moduloAtivo}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  irPara(p: number): void {
    if (p < 0 || p >= this.totalPages) return;
    this.consultar(p);
  }

  mudarTamanho(event: Event): void {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.consultar(0);
  }

  isStatus(val: any): boolean {
    return ['PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO'].includes(val);
  }

  get paginasVisiveis(): (number | null)[] {
    const total = this.totalPages;
    const atual = this.page;

    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i);
    }

    const set = new Set<number>([0, total - 1]);

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
      if (i > 0 && sorted[i] - sorted[i - 1] > 1) {
        result.push(null);
      }
      result.push(sorted[i]);
    }

    return result;
  }
}
