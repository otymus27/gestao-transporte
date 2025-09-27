// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { Login } from './components/layout-admin/login/login';
import { Principal } from './components/layout-admin/principal/principal';
import { AuthGuard } from './guards/auth.guard';
import { UsuarioComponent } from './components/usuario/usuario.component';
import { RedefinicaoSenhaComponent } from './components/redefinicao-senha/redefinicao-senha.component';
import { HomeComponentPublico } from './components/layout-publico/home/home.component';
import { ExplorerComponent } from './features/public/pages/explorer/explorer.component';

// Componentes Admin
import { AdminExplorerComponent } from './features/admin/pages/admin-explorer/admin-explorer.component';
import { DashboardComponent } from './features/admin/pages/dashboard/dashboard.component';
import { AlterarSenhaComponent } from './components/alterar-senha/alterar-senha.component';
import { ExplorerFormPublicComponent } from './features/public/pages/formularios/explorer-form-public.component';
import { AdminFormularioComponent } from './features/admin/pages/admin-formulario/admin-formulario.component';
import { FarmaciaExplorerPublicoComponent } from './features/public/pages/farmacia/farmacia.component';
import { SobreComponent } from './components/layout-admin/sobre/sobre.component';

export const routes: Routes = [
  // --- Rotas Públicas ---
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponentPublico },

  // 🔹 Página Sobre (público)
  { path: 'sobre', component: SobreComponent },

  // Explorer de Protocolos Públicos
  { path: 'publico', component: ExplorerComponent },

  // Explorer de Formulários Públicos
  { path: 'formularios-publicos', component: ExplorerFormPublicComponent },

  // 🚨 Farmácia Pública
  { path: 'farmacias', component: FarmaciaExplorerPublicoComponent },

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

      // Pastas (restrito ao ADMIN)
      {
        path: 'pastas',
        children: [
          {
            path: '',
            component: AdminExplorerComponent,
            data: { roles: ['ADMIN'] },
          },
          {
            path: 'gerenciar',
            component: AdminExplorerComponent,
            data: { roles: ['ADMIN'] },
          },
        ],
      },

      // Farmácia (restrito ao ADMIN), aqui é somente atalho para pasta dentro da pasta FARMACIA dentro de pastas de protocolos
      {
        path: 'farmacia',
        component: AdminExplorerComponent,
        data: { roles: ['ADMIN'], pastaId: 1 },
      },

      // Formulários (restrito ao ADMIN)
      {
        path: 'formularios',
        children: [
          {
            path: '',
            component: AdminFormularioComponent,
            data: { roles: ['ADMIN'] },
          },
          {
            path: 'gerenciar',
            component: AdminFormularioComponent,
            data: { roles: ['ADMIN'] },
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
