import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgApexchartsModule, ChartComponent } from 'ng-apexcharts';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexDataLabels,
  ApexStroke,
  ApexTitleSubtitle,
  ApexYAxis,
  ApexFill,
  ApexGrid,
} from 'ng-apexcharts';
import { Dashboard, DashboardService } from '../../services/dashboard.service';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  dataLabels: ApexDataLabels;
  stroke?: ApexStroke;
  fill?: ApexFill;
  grid?: ApexGrid;
  xaxis: ApexXAxis;
  yaxis?: ApexYAxis;
  title: ApexTitleSubtitle;
  colors: string[];
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgApexchartsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
  dashboard?: Dashboard;
  loading = true;
  lastUpdated: Date = new Date();
  private refreshInterval?: ReturnType<typeof setInterval>;

  @ViewChild('setorChart') setorChart!: ChartComponent;
  @ViewChild('motoristaChart') motoristaChart!: ChartComponent;
  @ViewChild('linhaChart') linhaChart!: ChartComponent;

  setoresOptions!: ChartOptions;
  motoristasOptions!: ChartOptions;
  solicitacoesDiaOptions!: ChartOptions;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.carregarDashboard();
    this.refreshInterval = setInterval(() => this.carregarDashboard(), 60000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  carregarDashboard(): void {
    this.loading = true;
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.configurarGraficos(data);
        this.loading = false;
        this.lastUpdated = new Date();
      },
      error: (err) => {
        console.error('Erro ao carregar dashboard', err);
        this.loading = false;
      },
    });
  }

  configurarGraficos(data: Dashboard): void {
    this.setoresOptions = {
      series: [
        {
          name: 'Solicitações',
          data: data.topSetores.map((s) => s.quantidade),
        },
      ],
      chart: { type: 'bar', height: 300, toolbar: { show: false } },
      colors: ['#0d6efd'],
      xaxis: {
        categories: data.topSetores.map((s) => s.nome),
        labels: { rotate: -30 },
      },
      dataLabels: { enabled: true },
      title: { text: '🏢 Top Setores', align: 'center' },
    };

    this.motoristasOptions = {
      series: [
        {
          name: 'Solicitações',
          data: data.topMotoristas.map((m) => m.quantidade),
        },
      ],
      chart: { type: 'bar', height: 300, toolbar: { show: false } },
      colors: ['#198754'],
      xaxis: {
        categories: data.topMotoristas.map((m) => m.nome),
        labels: { rotate: -30 },
      },
      dataLabels: { enabled: true },
      title: { text: '🚗 Top Motoristas', align: 'center' },
    };

    this.solicitacoesDiaOptions = {
      series: [
        {
          name: 'Solicitações',
          data: data.solicitacoesPorDia.map((s) => s.total),
        },
      ],
      chart: { type: 'area', height: 320, toolbar: { show: false } },
      stroke: { curve: 'smooth', width: 3 },
      colors: ['#6610f2'],
      fill: {
        type: 'gradient',
        gradient: {
          shadeIntensity: 1,
          opacityFrom: 0.5,
          opacityTo: 0.1,
          stops: [0, 90, 100],
        },
      },
      grid: {
        borderColor: '#e7e7e7',
        row: { colors: ['#f3f3f3', 'transparent'], opacity: 0.5 },
      },
      xaxis: {
        categories: data.solicitacoesPorDia.map((s) => s.data),
        labels: { rotate: -45 },
        title: { text: 'Data' },
      },
      yaxis: { title: { text: 'Qtd' } },
      title: {
        text: '📈 Solicitações por Dia (Últimos 7 dias)',
        align: 'center',
      },
      dataLabels: { enabled: false },
    };
  }

  get percentualConcluidas(): string {
    if (!this.dashboard || !this.dashboard.totalSolicitacoes) return '0';
    return (
      (this.dashboard.solicitacoesFinalizadas /
        this.dashboard.totalSolicitacoes) *
      100
    ).toFixed(1);
  }
}
