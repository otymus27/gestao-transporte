// src/app/app.routes.ts
import { Routes } from '@angular/router';

import { Principal } from './components/layout-admin/principal/principal';
import { AuthGuard } from './guards/auth.guard';
import { UsuarioComponent } from './components/usuario/usuario.component';
import { RedefinicaoSenhaComponent } from './components/redefinicao-senha/redefinicao-senha.component';

// Componentes Admin
import { DashboardComponent } from './features/admin/pages/dashboard/dashboard.component';
import { AlterarSenhaComponent } from './components/alterar-senha/alterar-senha.component';
import { SobreComponent } from './components/layout-admin/sobre/sobre.component';
import { Login } from './components/login/login';
import { SetorComponent } from './components/setor/setor.component';
import { DestinoComponent } from './components/destino/destino.component';
import { CarroComponent } from './components/carro/carro.component';
import { MotoristaComponent } from './components/motorista/motorista.component';
import { SolicitacaoComponent } from './components/solicitacao/solicitacao.component';

export const routes: Routes = [
  // --- Rotas Públicas ---

  { path: 'login', component: Login },
  { path: 'redefinir-senha', component: RedefinicaoSenhaComponent },

  // --- Rotas Administrativas (Protegidas) ---
  {
    path: 'admin',
    component: Principal,
    canActivate: [AuthGuard],
    children: [
      // Dashboard é a página inicial da área admin
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        component: DashboardComponent,
        // acessível para qualquer usuário logado
      },

      // 🔹 Carros
      {
        path: 'carros',
        children: [
          {
            path: '',
            component: CarroComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'gerenciar',
            component: CarroComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'novo',
            component: CarroComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // 🔹 Destinos
      {
        path: 'destinos',
        children: [
          {
            path: '',
            component: DestinoComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'gerenciar',
            component: DestinoComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'novo',
            component: DestinoComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // 🔹 Motoristas
      {
        path: 'motoristas',
        children: [
          {
            path: '',
            component: MotoristaComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'gerenciar',
            component: MotoristaComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'novo',
            component: MotoristaComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // 🔹 Setores
      {
        path: 'setores',
        children: [
          {
            path: '',
            component: SetorComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'gerenciar',
            component: SetorComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'novo',
            component: SetorComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // 🔹 Solicitações
      {
        path: 'solicitacoes',
        children: [
          {
            path: '',
            component: SolicitacaoComponent,
            data: { roles: ['ADMIN', 'GERENTE', 'BASIC'] },
          },
          {
            path: 'gerenciar',
            component: SolicitacaoComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
          {
            path: 'novo',
            component: SolicitacaoComponent,
            data: { roles: ['ADMIN', 'GERENTE'] },
          },
        ],
      },

      // Usuários (restrito ao ADMIN)
      {
        path: 'usuarios',
        children: [
          {
            path: '',
            component: UsuarioComponent,
            data: { roles: ['ADMIN'] },
          },
          {
            path: 'gerenciar',
            component: UsuarioComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // Rota para usuario logado redefinir sua propria senha
      {
        path: 'perfil',
        children: [
          {
            path: 'alterar-senha',
            component: AlterarSenhaComponent,
            data: { roles: ['ADMIN', 'GERENTE', 'BASIC'] }, // todos os logados podem alterar
          },
        ],
      },

      // 🔹 Página Sobre (admin)
      {
        path: 'sobre',
        component: SobreComponent,
        data: { roles: ['ADMIN', 'GERENTE', 'BASIC'] }, // todos os logados
      },
    ],
  },

  // Rotas inválidas → login
  { path: '**', redirectTo: 'login' },
];
