// app.component.ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // Geralmente usado para renderizar rotas
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  // Se você tiver um template básico, talvez apenas <router-outlet></router-outlet>
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  // 'imports' e 'providers' não são necessários aqui se você estiver usando AppModule
  // RouterOutlet pode ser importado aqui se o AppComponent o utilizar diretamente
  imports: [RouterOutlet], // Manter RouterOutlet aqui se ele for usado no template do AppComponent
})
export class AppComponent {
  title = 'app-portal';
  env = environment; // expõe no template se quiser mostrar

  ngOnInit() {
    console.log('🔎 Ambiente carregado:', environment);

    // Para testar no console do navegador:
    (window as any).envTest = environment;
  }
}
