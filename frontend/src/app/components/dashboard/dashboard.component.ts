import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private authService = inject(AuthService);

  totalSolicitacoes = 0;
  totalMotoristas = 0;
  totalCarros = 0;
  totalSetores = 0;
  totalFichas = 0;

  carregando = true;

  atalhos = [
    {
      titulo: 'Carros',
      descricao: 'Gerenciar veículos cadastrados',
      rota: '/admin/carros/gerenciar',
      icon: 'bi bi-car-front-fill',
      cor: 'blue',
      totalKey: 'totalCarros'
    },
    {
      titulo: 'Motoristas',
      descricao: 'Consultar e editar motoristas',
      rota: '/admin/motoristas/gerenciar',
      icon: 'bi bi-person-vcard-fill',
      cor: 'green',
      totalKey: 'totalMotoristas'
    },
    {
      titulo: 'Setores',
      descricao: 'Gerenciar setores da instituição',
      rota: '/admin/setores/gerenciar',
      icon: 'bi bi-building-fill',
      cor: 'orange',
      totalKey: 'totalSetores'
    },
    {
      titulo: 'Solicitações',
      descricao: 'Acompanhar solicitações do sistema',
      rota: '/admin/solicitacoes/gerenciar',
      icon: 'bi bi-journal-text',
      cor: 'purple',
      totalKey: 'totalSolicitacoes'
    },
    {
      titulo: 'Fichas',
      descricao: 'Acessar fichas cadastradas',
      rota: '/admin/fichas/gerenciar',
      icon: 'bi bi-journal-bookmark-fill',
      cor: 'teal',
      totalKey: 'totalFichas'
    }
  ];

  ngOnInit(): void {
    this.carregarDashboard();
  }

  carregarDashboard(): void {
    this.carregando = true;

    this.dashboardService.getDashboard().subscribe({
      next: (res: any) => {
        this.totalSolicitacoes = res.totalSolicitacoes ?? 0;
        this.totalMotoristas = res.totalMotoristas ?? 0;
        this.totalCarros = res.totalCarros ?? 0;
        this.totalSetores = res.totalSetores ?? 0;
        this.totalFichas = res.totalFichas ?? 0;
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
      }
    });
  }

  getTotal(item: any): number {
    return this[item.totalKey as keyof DashboardComponent] as number ?? 0;
  }

  isAdmin(): boolean {
    return this.authService.getLoggedInRoles().includes('ADMIN');
  }

  isGerente(): boolean {
    return this.authService.getLoggedInRoles().includes('GERENTE');
  }

  isSupervisor(): boolean {
    return this.authService.getLoggedInRoles().includes('SUPERVISOR');
  }

  isBasic(): boolean {
    return this.authService.getLoggedInRoles().includes('BASIC');
  }
}
