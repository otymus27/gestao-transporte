import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './../sidebar/sidebar.component.html',
  styleUrl: './../sidebar/sidebar.component.scss',
})
export class SidebarComponent {
  private authService = inject(AuthService);

  isCollapsed = false;

  // estados dos submenus
  showPastasSubmenu = false;
  showFormulariosSubmenu = false;
  showUsuariosSubmenu = false;
  showSetoresSubmenu = false;

  toggle() {
    this.isCollapsed = !this.isCollapsed;
  }

  toggleSubmenu(submenu: string) {
    if (submenu === 'pastas') {
      this.showPastasSubmenu = !this.showPastasSubmenu;
      this.showFormulariosSubmenu = false;
      this.showUsuariosSubmenu = false;
    } else if (submenu === 'formularios') {
      this.showFormulariosSubmenu = !this.showFormulariosSubmenu;
      this.showPastasSubmenu = false;
      this.showUsuariosSubmenu = false;
    } else if (submenu === 'usuarios') {
      this.showUsuariosSubmenu = !this.showUsuariosSubmenu;
      this.showPastasSubmenu = false;
      this.showFormulariosSubmenu = false;
    } else if (submenu === 'setores') {
      this.showSetoresSubmenu = !this.showSetoresSubmenu; // 🔹 controle do submenu
    }
  }

  hideAllSubmenus() {
    this.showPastasSubmenu = false;
    this.showFormulariosSubmenu = false;
    this.showUsuariosSubmenu = false;
    this.showSetoresSubmenu = false;
  }

  // Roles
  isAdmin(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('ADMIN');
  }
  isGerente(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('GERENTE');
  }
  isBasic(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('BASIC');
  }
  isLogado(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.length > 0;
  }
}
