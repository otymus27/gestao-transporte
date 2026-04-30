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

  toggle() {
    this.isCollapsed = !this.isCollapsed;
  }

  isAdmin(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('ADMIN');
  }

  isGerente(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('GERENTE');
  }

  isSupervisor(): boolean {
    const roles = this.authService.getLoggedInRoles();
    return roles.includes('SUPERVISOR');
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
