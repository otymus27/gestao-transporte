import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sobre',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sobre.component.html',
  styleUrls: ['./sobre.component.scss']
})
export class SobreComponent {
  arr(n: number) {
    return Array.from({ length: n });
  }

  stack = [
    {
      nome: 'Spring Boot',
      role: 'Framework backend',
      version: '3.x',
      icon: '🍃',
      cor: 'green',
      pct: 95,
    },
    {
      nome: 'Java',
      role: 'Linguagem backend',
      version: '21 LTS',
      icon: '☕',
      cor: 'orange',
      pct: 95,
    },
    {
      nome: 'Angular',
      role: 'Framework frontend',
      version: '18',
      icon: '🅰️',
      cor: 'red',
      pct: 90,
    },
    {
      nome: 'MySQL',
      role: 'Banco de dados',
      version: '8.x',
      icon: '🐬',
      cor: 'blue',
      pct: 88,
    },
    {
      nome: 'Spring Security',
      role: 'Autenticação JWT',
      version: '6.x',
      icon: '🔐',
      cor: 'purple',
      pct: 85,
    },
    {
      nome: 'Relatórios',
      role: 'Exportação PDF/Excel',
      version: '7.x',
      icon: '📄',
      cor: 'amber',
      pct: 80,
    },
    {
      nome: 'JPA / Hibernate',
      role: 'ORM e persistência',
      version: '6.x',
      icon: '🗄️',
      cor: 'teal',
      pct: 88,
    },
  ];

  features = [
    {
      icon: '📋',
      cor: 'blue',
      titulo: 'Gestão de Registros',
      desc: 'Controle completo dos módulos do sistema com interface moderna e organizada.',
    },
    {
      icon: '⏰',
      cor: 'amber',
      titulo: 'Acompanhamento',
      desc: 'Visualização rápida do andamento dos registros e status operacionais.',
    },
    {
      icon: '📊',
      cor: 'green',
      titulo: 'Relatórios',
      desc: 'Exportação de dados e acompanhamento por filtros para análise administrativa.',
    },
    {
      icon: '🏢',
      cor: 'teal',
      titulo: 'Estrutura Administrativa',
      desc: 'Organização por setores, usuários, permissões e módulos institucionais.',
    },
    {
      icon: '👥',
      cor: 'purple',
      titulo: 'Perfis de Acesso',
      desc: 'Controle por perfil para restringir ações conforme o nível do usuário.',
    },
    {
      icon: '🔒',
      cor: 'red',
      titulo: 'Segurança',
      desc: 'Autenticação e proteção de rotas para maior segurança da aplicação.',
    },
  ];

  perfis = [
    {
      nome: 'Administrador',
      role: 'ROLE_ADMIN',
      icon: '👑',
      cor: 'dark',
      perms: [
        { label: 'Acesso total ao sistema', ok: true },
        { label: 'Gerenciar usuários', ok: true },
        { label: 'Gerenciar setores', ok: true },
        { label: 'Gerar relatórios', ok: true },
        { label: 'Excluir registros', ok: true },
      ],
    },
    {
      nome: 'Gerente',
      role: 'ROLE_GERENTE',
      icon: '🧑‍💼',
      cor: 'indigo',
      perms: [
        { label: 'Gerenciar módulos permitidos', ok: true },
        { label: 'Cadastrar registros', ok: true },
        { label: 'Gerar relatórios', ok: true },
        { label: 'Gerenciar usuários', ok: false },
        { label: 'Excluir registros', ok: false },
      ],
    },
    {
      nome: 'Básico',
      role: 'ROLE_BASIC',
      icon: '👤',
      cor: 'blue',
      perms: [
        { label: 'Visualizar módulos', ok: true },
        { label: 'Criar registros permitidos', ok: true },
        { label: 'Gerar relatórios básicos', ok: true },
        { label: 'Gerenciar usuários', ok: false },
        { label: 'Excluir registros', ok: false },
      ],
    },
  ];

  changelog = [
    {
      versao: 'v 2.0.0',
      data: 'Atual',
      atual: true,
      titulo: 'Nova identidade visual',
      items: [
        'Hero section redesenhada',
        'Cards premium com visual institucional',
        'Seções com layout moderno',
        'Página sobre alinhada ao novo dashboard',
      ],
    },
    {
      versao: 'v 1.5.0',
      data: 'Anterior',
      atual: false,
      titulo: 'Melhorias estruturais',
      items: [
        'Padronização dos componentes',
        'Ajustes de responsividade',
        'Refino visual da navegação',
      ],
    },
    {
      versao: 'v 1.0.0',
      data: 'Inicial',
      atual: false,
      titulo: 'Primeira versão',
      items: [
        'Estrutura base da aplicação',
        'Módulos administrativos',
        'Autenticação e navegação',
      ],
    },
  ];
}