import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'; // Importe o HttpClient
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  // ✅ Adicione esta propriedade
  senhaProvisoria?: boolean;
}
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  http = inject(HttpClient);
  router = inject(Router);

  // ✅ Ajuste a URL para o prefixo da sua API que o Nginx irá redirecionar
  //private readonly API_URL = '/api/login';
  // private readonly API_URL = 'http://localhost:8082/login'; // ⚠️ Ajuste para a URL real do seu backend
  // ✅ Agora a URL é carregada do arquivo de ambiente
  private readonly API_URL = environment.apiUrl;

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USERNAME_KEY = 'logged_username';
  private readonly ROLES_KEY = 'logged_roles';
  private readonly FULLNAME_KEY = 'logged_fullname';

  // ✅ BehaviorSubject para emitir o estado de login e informações do usuário
  private _isLoggedIn = new BehaviorSubject<boolean>(this.hasToken());
  isLoggedIn$ = this._isLoggedIn.asObservable();

  private _loggedInUsername = new BehaviorSubject<string | null>(
    localStorage.getItem(this.USERNAME_KEY)
  );
  loggedInUsername$ = this._loggedInUsername.asObservable();

  private _loggedInRoles = new BehaviorSubject<string[]>(
    JSON.parse(localStorage.getItem(this.ROLES_KEY) || '[]')
  );
  loggedInRoles$ = this._loggedInRoles.asObservable();

  private _loggedInFullName = new BehaviorSubject<string | null>(
    localStorage.getItem(this.FULLNAME_KEY)
  );
  loggedInFullName$ = this._loggedInFullName.asObservable(); // ✅ observable para o header

  // Injetando o HttpClient no construtor
  constructor() {}

  // Verifica se o token existe no localStorage
  private hasToken(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  // Define o token no localStorage e atualiza o estado de login
  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.extractUserInfoFromToken(token); // ✅ Extrai e armazena info do usuário
    this._isLoggedIn.next(true);
    this.carregarUsuarioLogado()
    console.log('Token salvo:', token);
  }

  // Retorna o token do localStorage
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  // ✅ NOVO: Extrai username e roles do token JWT
  private extractUserInfoFromToken(token: string): void {
    try {
      const payloadBase64 = token.split('.')[1];
      const payload = JSON.parse(atob(payloadBase64)); // Decodifica Base64 e faz parse do JSON

      const username = payload.sub; // 'sub' é o padrão para o nome do usuário
      const roles = payload.roles; // 'roles' é a claim que você usa para as roles

      localStorage.setItem(this.USERNAME_KEY, username);
      localStorage.setItem(this.ROLES_KEY, JSON.stringify(roles));

      this._loggedInUsername.next(username);
      this._loggedInRoles.next(roles);
    } catch (e) {
      console.error('Falha ao decodificar token JWT:', e);
      this.clearSession(); // Limpa a sessão se o token for inválido
    }
  }

  // ✅ NOVO: Retorna o nome do usuário logado
  getLoggedInUsername(): string | null {
    return this._loggedInUsername.getValue();
  }

  // ✅ NOVO: Retorna as roles do usuário logado
  getLoggedInRoles(): string[] {
    return this._loggedInRoles.getValue();
  }

  // ✅ NOVO: Retorna o nome completo do usuário logado
  getLoggedInFullName(): string | null {
    return this._loggedInFullName.getValue();
  }

  // ✅ Retorna apenas o primeiro nome
  getLoggedInFirstName(): string | null {
    const fullName = this._loggedInFullName.getValue();
    if (!fullName) return null;
    return fullName.split(' ')[0]; // pega só o primeiro nome
  }

  // ✅ Retorna a role principal (primeira da lista)
  getLoggedInMainRole(): string | null {
    const roles = this._loggedInRoles.getValue();
    return roles.length > 0 ? roles[0] : null;
  }


  // ✅ Chama o endpoint /usuario/logado
  private carregarUsuarioLogado(): void {
    this.http.get<any>(`${this.API_URL}/usuario/logado`).subscribe({
      next: (usuario) => {
        console.log('📌 Usuário logado recebido do backend:', usuario);

        const roles = (usuario.roles || []).map((r: any) =>
          typeof r === 'string' ? r : r.nome
        );

        localStorage.setItem(this.USERNAME_KEY, usuario.username);
        localStorage.setItem(this.ROLES_KEY, JSON.stringify(roles));
        localStorage.setItem(this.FULLNAME_KEY, usuario.nomeCompleto);

        this._loggedInUsername.next(usuario.username);
        this._loggedInRoles.next(roles);
        this._loggedInFullName.next(usuario.nomeCompleto);

        console.log('✅ Nome completo salvo:', usuario.nomeCompleto);
        console.log('✅ Roles salvas:', roles);
      },
      error: (err) => {
        console.error('Erro ao buscar usuário logado:', err);
        this.clearSession();
      },
    });
  }


  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USERNAME_KEY); // ✅ Remove info do usuário
    localStorage.removeItem(this.ROLES_KEY); // ✅ Remove info do usuário
    localStorage.removeItem(this.FULLNAME_KEY); // ✅ Remove info do usuário

    this._isLoggedIn.next(false);
    this._loggedInUsername.next(null);
    this._loggedInRoles.next([]);
    this._loggedInFullName.next(null);
  }

  // Envia as credenciais de login para o backend
  login(credenciais: any): Observable<any> {
    return this.http
      .post<AuthResponse>(`${this.API_URL}/login`, credenciais)
      .pipe(
        tap((response: AuthResponse) => {
          // ✅ Apenas chama setToken() e deixa a lógica de extração lá
          if (response.accessToken) {
            this.setToken(response.accessToken);
          }

          // ✅ Adicione esta linha para depuração
          console.log('Resposta completa do backend:', response);
          console.log('Valor de senhaProvisoria:', response.senhaProvisoria);
          // ✅ Adicione esta verificação
          if (response.senhaProvisoria) {
            console.log(
              'Detectado senha provisória. Tentando navegar para /redefinir-senha...'
            );
            setTimeout(() => {
              this.router.navigateByUrl('/redefinir-senha').then((success) => {
                if (success) {
                  console.log('Navegação para redefinir-senha bem-sucedida!');
                } else {
                  console.error('Falha na navegação para redefinir-senha.');
                }
              });
            }, 100);
          } else {
            console.log(
              'Login bem-sucedido. Redirecionando para a área principal.'
            );
            this.router.navigate(['/admin/dashboard']);
          }
        }),
        map(() => true)
      );
  }

  // Limpa o token e todas as informações do usuário do localStorage e redireciona para o login
  logout(): void {
    const token = this.getToken();
  
    if (token) {
      this.http.post(`${this.API_URL}/logout`, {}, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).subscribe({
        next: () => {
          console.log('Logout registrado no backend');
          this.clearSession();
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error('Erro ao chamar logout no backend:', err);
          // Mesmo se der erro, limpa localmente
          this.clearSession();
          this.router.navigate(['/login']);
        }
      });
    } else {
      // Caso não tenha token, só limpa local
      this.clearSession();
      this.router.navigate(['/login']);
    }
  }
}
