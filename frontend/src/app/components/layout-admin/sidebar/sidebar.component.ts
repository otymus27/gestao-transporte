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

  // controla qual submenu está aberto
  currentOpen: string | null = null;

  toggle() {
    this.isCollapsed = !this.isCollapsed;
  }

  toggleSubmenu(submenu: string) {
    if (this.currentOpen === submenu) {
      // se clicar no mesmo já aberto → fecha
      this.currentOpen = null;
    } else {
      // abre o novo e fecha os outros
      this.currentOpen = submenu;
    }
  }

  isSubmenuOpen(submenu: string): boolean {
    return this.currentOpen === submenu;
  }

  //fecha todos menus e submenus
  hideAllSubmenus() {
    this.currentOpen = null;
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
