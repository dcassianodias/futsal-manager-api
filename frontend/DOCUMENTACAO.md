# Futsal Manager UI — Documentação Técnica

> Angular 19 • Standalone Components • PWA

---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [Estrutura de Pastas](#2-estrutura-de-pastas)
3. [PWA — Progressive Web App](#3-pwa--progressive-web-app)
4. [Roteamento](#4-roteamento)
5. [Shell da Aplicação (app.component)](#5-shell-da-aplicação-appcomponent)
6. [Modelos de Dados](#6-modelos-de-dados)
7. [Services (Serviços HTTP)](#7-services-serviços-http)
8. [Páginas — Públicas](#8-páginas--públicas)
9. [Páginas — Administrativas](#9-páginas--administrativas)
10. [Sistema de Estilos](#10-sistema-de-estilos)
11. [Configuração do Ambiente](#11-configuração-do-ambiente)
12. [Modelo SaaS — Como monetizar](#12-modelo-saas--como-monetizar)
13. [Roadmap Técnico Sugerido](#13-roadmap-técnico-sugerido)

---

## 1. Visão Geral

O **Futsal Manager UI** é o frontend da plataforma Futsal Manager, construído em Angular 19 com arquitetura de **Standalone Components** — sem NgModules, cada componente é independente e auto-suficiente.

A plataforma tem dois universos distintos:

| Universo | URL | Quem acessa |
|---|---|---|
| **Público** | `/times`, `/time/:codigo` | Qualquer pessoa, sem login |
| **Administrativo** | `/dashboard`, `/jogos`, etc. | Admin do time (futuro login) |

O backend é um **Spring Boot 3** com PostgreSQL, expondo a API em `http://localhost:8080`.

---

## 2. Estrutura de Pastas

```
src/
├── app/
│   ├── app.component.ts/html/css   ← Shell principal (sidebar + router)
│   ├── app.config.ts               ← Providers globais (HTTP, Router, PWA)
│   ├── app.routes.ts               ← Todas as rotas da aplicação
│   │
│   ├── config/
│   │   └── constants.ts            ← API_URL, TIME_ID, função fmt()
│   │
│   ├── models/                     ← Interfaces TypeScript (contratos de dados)
│   │   ├── time.model.ts
│   │   ├── jogo.model.ts
│   │   ├── usuario.model.ts
│   │   ├── pagamento.model.ts
│   │   ├── evento.model.ts
│   │   └── despesa.model.ts
│   │
│   ├── services/                   ← Camada de comunicação com a API
│   │   ├── time.service.ts
│   │   ├── jogo.service.ts
│   │   ├── usuario.service.ts
│   │   ├── pagamento.service.ts
│   │   ├── evento.service.ts
│   │   └── despesa.service.ts
│   │
│   └── pages/                      ← Um componente por tela
│       ├── times/                  ← Listagem pública de times (PWA)
│       ├── time-publico/           ← Página pública de cada time
│       ├── dashboard/              ← Painel admin com estatísticas
│       ├── jogos/                  ← Gestão de jogos
│       ├── usuarios/               ← Gestão de jogadores
│       ├── pagamentos/             ← Gestão financeira
│       ├── eventos/                ← Gestão de eventos
│       └── despesas/               ← Gestão de despesas
│
├── styles.css                      ← Design system global (variáveis CSS)
├── index.html                      ← HTML raiz com meta tags PWA
└── main.ts                         ← Bootstrap da aplicação

public/
└── manifest.webmanifest            ← Manifesto PWA (nome, ícones, tema)

ngsw-config.json                    ← Configuração do Service Worker Angular
```

---

## 3. PWA — Progressive Web App

### O que é PWA?
PWA (Progressive Web App) é uma tecnologia que permite que um site seja instalado no celular ou desktop como se fosse um aplicativo nativo. O usuário pode:
- Adicionar à tela inicial do celular
- Usar offline (conteúdo cacheado)
- Receber notificações push (futuro)
- Ter experiência de app sem passar pela App Store

### Como foi implementado

**`@angular/service-worker`** — pacote Angular que gerencia o Service Worker:
```json
// package.json
"@angular/service-worker": "^19.2.0"
```

**`app.config.ts`** — registra o service worker ao inicializar o app:
```typescript
provideServiceWorker('ngsw-worker.js', {
  enabled: !isDevMode(),          // só ativo em produção (ng build)
  registrationStrategy: 'registerWhenStable:30000'  // registra após 30s de estabilidade
})
```

**`ngsw-config.json`** — define o que o service worker faz:
- `app-shell`: cacheia os arquivos principais (HTML, JS, CSS) para uso offline
- `assets`: cacheia imagens e fontes de forma lazy (só quando requisitados)

**`public/manifest.webmanifest`** — descreve o app para o browser/SO:
- Nome do app, nome curto, ícone, cor do tema
- `start_url: "/times"` — tela que abre ao instalar o app
- `display: "standalone"` — remove a barra de URL do browser

**`src/index.html`** — conecta o HTML ao manifesto:
```html
<meta name="theme-color" content="#0f172a">
<meta name="apple-mobile-web-app-capable" content="yes">
<link rel="manifest" href="manifest.webmanifest">
```

> **Para ativar o PWA:** rode `ng build` (não `ng serve`) e sirva a pasta `dist/` com um servidor HTTP (ex: `npx serve dist/futsal-manager-ui/browser`).

---

## 4. Roteamento

**Arquivo:** `src/app/app.routes.ts`

O Angular Router é a biblioteca que mapeia URLs para componentes.

```typescript
export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'times',         loadComponent: () => import('./pages/times/...')         },
  { path: 'time/:codigo',  loadComponent: () => import('./pages/time-publico/...') },
  { path: 'dashboard',     loadComponent: () => import('./pages/dashboard/...')    },
  { path: 'jogos',         loadComponent: () => import('./pages/jogos/...')        },
  // ...
];
```

### Conceitos usados

**`loadComponent` (Lazy Loading)**
Em vez de carregar todos os componentes ao abrir o app, cada página é carregada só quando o usuário navegar para ela. Isso reduz o tempo de carregamento inicial.

**`path: ':codigo'` (Parâmetro de rota)**
O `:codigo` é uma variável na URL. Quando o usuário acessa `/time/FLM123`, o valor `FLM123` fica disponível no componente via `ActivatedRoute`.

**`redirectTo`**
Quando o usuário acessa `/` (raiz), o router redireciona automaticamente para `/dashboard`.

### Como o componente lê o parâmetro

```typescript
// time-publico.component.ts
constructor(private route: ActivatedRoute) {}

ngOnInit() {
  const codigo = this.route.snapshot.paramMap.get('codigo'); // ex: "FLM123"
}
```

---

## 5. Shell da Aplicação (app.component)

**Arquivo:** `src/app/app.component.ts` + `.html` + `.css`

É o componente raiz. Ele envolve todas as páginas e decide se mostra ou não a sidebar.

### Detecção de rota pública

```typescript
isPublic = toSignal(
  this.router.events.pipe(
    filter(e => e instanceof NavigationEnd),
    map((e: NavigationEnd) => this.checkPublic(e.urlAfterRedirects)),
    startWith(this.checkPublic(this.router.url))
  ),
  { initialValue: this.checkPublic(this.router.url) }
);

private checkPublic(url: string): boolean {
  return url === '/times' || url.startsWith('/time/');
}
```

**`toSignal`** — converte um Observable (stream de eventos) em um Signal Angular (valor reativo). Quando o valor muda, o template atualiza automaticamente.

**`NavigationEnd`** — evento emitido pelo Router sempre que a navegação conclui.

**`filter / map / startWith`** — operadores RxJS:
- `filter`: deixa passar só eventos do tipo `NavigationEnd`
- `map`: transforma o evento em `boolean` (é pública?)
- `startWith`: emite o valor inicial imediatamente (para a primeira renderização)

### Template condicional

```html
<div class="app-shell" [class.no-sidebar]="isPublic()">
  @if (!isPublic()) {
    <aside class="sidebar">...</aside>
  }
  <main class="main-content">
    <router-outlet></router-outlet>
  </main>
</div>
```

**`@if`** — nova sintaxe de controle de fluxo do Angular 17+. Substitui o antigo `*ngIf`.

**`[class.no-sidebar]`** — adiciona/remove a classe CSS `no-sidebar` baseado no signal.

**`<router-outlet>`** — o "buraco" onde o Angular injeta o componente da rota atual.

**`routerLink` / `routerLinkActive`** — diretivas Angular para navegação SPA (sem recarregar a página) e para marcar o link ativo na sidebar.

---

## 6. Modelos de Dados

**Pasta:** `src/app/models/`

São interfaces TypeScript que descrevem o formato dos dados retornados pela API.

### `time.model.ts`
```typescript
export interface Time {
  id: string;            // UUID — identificador único
  nome: string;          // "Futsal Estrela"
  valorMensalidade?: number;  // ? = campo opcional
  ativo: boolean;        // true = time ativo
  codigo: string;        // "FUT001" — usado na URL pública
  dataCriacao: string;   // ISO 8601: "2025-01-15T10:30:00"
  dataAtualizacao: string;
}
```

### `jogo.model.ts`
```typescript
export type StatusJogo = 'AGENDADO' | 'FINALIZADO' | 'CANCELADO';

export interface Jogo {
  id: string;
  timeId: string;
  adversario: string;
  local: string;
  dataHora: string;       // ISO 8601
  statusJogo: StatusJogo; // union type: só aceita esses 3 valores
  observacoes?: string;
}
```

### Por que usar interfaces?
TypeScript garante em tempo de compilação que você não acessa campos que não existem. Se a API mudar, o compilador avisa imediatamente em todo o projeto.

---

## 7. Services (Serviços HTTP)

**Pasta:** `src/app/services/`

Services são classes singleton (instanciadas uma única vez) responsáveis pela comunicação com a API.

### `time.service.ts`

```typescript
@Injectable({ providedIn: 'root' })  // singleton global
export class TimeService {
  private api = `${API_URL}/api/time/v1`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Time[]> {
    return this.http.get<Time[]>(this.api);  // GET /api/time/v1
  }

  create(data: TimeCreateRequest): Observable<Time> {
    return this.http.post<Time>(this.api, data);  // POST /api/time/v1
  }

  desativar(id: string): Observable<Time> {
    return this.http.patch<Time>(`${this.api}/${id}/desativar`, {});
  }
}
```

**`@Injectable({ providedIn: 'root' })`** — registra o service no injetor raiz. Qualquer componente pode injetá-lo no construtor.

**`HttpClient`** — cliente HTTP do Angular. Retorna sempre um `Observable`.

**`Observable<Time[]>`** — stream assíncrono que emite um valor (ou erro) quando a requisição HTTP conclui. É necessário chamar `.subscribe()` para executar a requisição.

### Como um componente usa o service

```typescript
constructor(private timeService: TimeService) {}

ngOnInit(): void {
  this.timeService.findAll().subscribe({
    next: times => { this.times = times; },  // sucesso
    error: err  => { this.erro = '...'; }    // falha
  });
}
```

---

## 8. Páginas — Públicas

### `TimesComponent` — `/times`

Listagem pública de todos os times ativos. Ponto de entrada do PWA.

**Funcionalidades:**
- Carrega todos os times via `TimeService.findAll()`
- Filtra apenas `ativo === true`
- Exibe cards com avatar colorido (cor gerada a partir do nome)
- Loading, erro e estado vazio tratados
- Cada card navega para `/time/:codigo`

**Geração de cor do avatar:**
```typescript
corAvatar(nome: string): string {
  const cores = ['#22c55e', '#3b82f6', ...];
  let hash = 0;
  for (let i = 0; i < nome.length; i++)
    hash = nome.charCodeAt(i) + ((hash << 5) - hash);
  return cores[Math.abs(hash) % cores.length];
}
```
A função cria um hash numérico do nome e seleciona uma cor da paleta. O mesmo time sempre terá a mesma cor.

---

### `TimePublicoComponent` — `/time/:codigo`

Página pública de um time específico. É a tela que o atleta instalaria no celular como PWA.

**Funcionalidades:**
- Lê o `codigo` da URL via `ActivatedRoute`
- Busca todos os times, filtra pelo código
- Carrega os jogos do time via `JogoService.findByTime(timeId)`
- Exibe **Próximos Jogos** (status AGENDADO, ordenados por data)
- Exibe **Últimos Resultados** (status FINALIZADO, mais recentes primeiro)
- Botão de voltar para `/times`

**Cálculo de "dias até o jogo":**
```typescript
diasAte(dateStr: string): string {
  const diff = new Date(dateStr).getTime() - Date.now();
  const dias = Math.ceil(diff / (1000 * 60 * 60 * 24));
  if (dias === 0) return 'hoje';
  if (dias === 1) return 'amanhã';
  return `em ${dias} dias`;
}
```

---

## 9. Páginas — Administrativas

Todas as páginas admin usam a sidebar e são acessíveis apenas por `/dashboard`, `/jogos`, etc. (futuro: protegidas por login).

### `DashboardComponent`
- Carrega 4 estatísticas em paralelo via `forkJoin`: jogos agendados, jogadores ativos, pagamentos pendentes, despesas do mês
- Exibe o próximo jogo agendado
- Lista os pagamentos pendentes recentes

**`forkJoin`** (RxJS): executa múltiplos Observables simultaneamente e espera todos completarem antes de emitir o resultado.

### `JogosComponent`
- CRUD completo: criar, visualizar, finalizar, cancelar
- Filtros por status (AGENDADO / FINALIZADO / CANCELADO)
- Formulário em slide panel (painel deslizante da direita)
- Status controlados no backend via PATCH (`/finalizar`, `/cancelar`)

### `UsuariosComponent` (Jogadores)
- CRUD: criar, desativar, reativar
- Filtro por status (Ativos / Inativos)
- Perfis: ADMIN ou ATLETA
- Tratamento de conflito 409 (e-mail duplicado)

### `PagamentosComponent`
- Dois tipos: MENSALIDADE (por mês de referência) e EVENTO (vinculado a um evento)
- Ações: pagar, cancelar
- Geração em lote de mensalidades para todos os jogadores ativos
- Resumo financeiro: total pendente, total pago

### `EventosComponent`
- Criação de eventos com data início/fim e valor sugerido
- Ativar / desativar eventos
- Layout em grid de cards

### `DespesasComponent`
- Registro de despesas por categoria (ALUGUEL_QUADRA, UNIFORME, EVENTO, OUTROS)
- Resumo: total geral e total do mês atual
- Exclusão de despesas

---

## 10. Sistema de Estilos

**Arquivo:** `src/styles.css`

O design inteiro é construído sobre **CSS Custom Properties** (variáveis CSS), definidas em `:root`.

### Paleta de cores
```css
--primary: #22c55e;       /* verde principal */
--danger: #ef4444;        /* vermelho */
--warning: #f59e0b;       /* amarelo */
--info: #3b82f6;          /* azul */
--sidebar-bg: #0f172a;    /* fundo escuro da sidebar */
--bg: #f8fafc;            /* fundo cinza claro das páginas */
```

### Classes utilitárias disponíveis
- **Botões:** `.btn`, `.btn-primary`, `.btn-danger`, `.btn-ghost`, `.btn-outline`, `.btn-sm`
- **Badges:** `.badge-green`, `.badge-blue`, `.badge-amber`, `.badge-red`, `.badge-gray`, `.badge-purple`
- **Tabelas:** `table`, `.table-wrapper`
- **Cards:** `.card`, `.card-header`, `.card-body`
- **Alertas:** `.alert-error`, `.alert-success`, `.alert-info`
- **Formulários:** `.form-group`, `.form-row`
- **Slide panel:** `.slide-panel`, `.panel-header`, `.panel-body`, `.panel-footer`
- **Layouts:** `.page`, `.page-header`, `.page-title`, `.page-actions`
- **Filtros:** `.filter-tabs`, `.filter-tab`
- **Estados:** `.empty-state`, `.loading-state`

---

## 11. Configuração do Ambiente

**Arquivo:** `src/app/config/constants.ts`

```typescript
export const API_URL = 'http://localhost:8080';  // URL base do backend
export const TIME_ID = '17628d2e-...';           // UUID do time admin atual

export function fmt(value: number): string {
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
```

> **Importante:** `TIME_ID` é temporário. Quando o login for implementado, esse valor virá do JWT decodificado.

### Comandos

```bash
# Desenvolvimento (sem PWA ativo)
npm start              # ou: ng serve

# Produção (com PWA ativo)
ng build
npx serve dist/futsal-manager-ui/browser

# Instalar dependência PWA (necessário uma vez)
npm install
```

---

## 12. Modelo SaaS — Como monetizar

A arquitetura atual (multi-time com `codigo` único por URL) é a base perfeita para um SaaS.

### Proposta de valor
> Cada time paga uma mensalidade para ter sua própria "mini-plataforma" gerenciada e uma URL pública para os atletas acompanharem tudo pelo celular como PWA.

### Estrutura de planos sugerida

| Plano | Preço/mês | Limites | Público-alvo |
|---|---|---|---|
| **Gratuito** | R$ 0 | 1 admin, 15 jogadores, 20 jogos/mês | Times amadores |
| **Starter** | R$ 29 | 2 admins, 30 jogadores, ilimitado | Times organizados |
| **Pro** | R$ 79 | 5 admins, ilimitado, relatórios PDF, notificações push | Ligas e escolinhas |
| **Liga** | R$ 199 | Multi-times, painel de campeonato, placar ao vivo | Organizadores de torneios |

### Funcionalidades que agregam valor (monetizáveis)

1. **Notificações push** — lembrete de jogo via service worker PWA
2. **Placar ao vivo** — admin atualiza em tempo real, atleta vê no PWA
3. **Relatórios em PDF** — demonstrativo financeiro, lista de jogadores
4. **Convocação de jogos** — admin convoca, atleta confirma presença no app
5. **Galeria de fotos por jogo** — upload de fotos pós-jogo
6. **Ranking de presença** — gamificação da frequência dos atletas
7. **Integração de pagamento** — cobrança de mensalidade via Pix (Mercado Pago/Stripe)
8. **Domínio personalizado** — `meufutsal.com.br` em vez de `/time/FLM001`
9. **Painel de campeonato** — chaveamento, tabela de classificação

### Stack sugerida para crescer

- **Autenticação:** Spring Security com JWT (já parcialmente no backend)
- **Multi-tenancy:** já existe via `time_id` em todas as tabelas
- **Billing:** Stripe ou Asaas (Brasil)
- **Notificações push:** Firebase Cloud Messaging + Angular service worker
- **Infraestrutura:** Railway ou Render (backend) + Vercel/Netlify (frontend) — custo baixo no início
- **Analytics:** PostHog ou Amplitude para entender uso por plano

### Funil de aquisição

1. Time descobre via SEO (página pública `futsal.app/time/:codigo`)
2. Atleta instala o PWA no celular
3. Admin do time vira cliente pagante para ter mais funcionalidades
4. Word-of-mouth: outros times da liga percebem e querem o mesmo

---

## 13. Roadmap Técnico Sugerido

### Fase 1 — Atual (base funcional)
- [x] PWA configurado
- [x] Listagem pública de times
- [x] Página pública por time com próximos jogos
- [x] CRUD admin: jogos, jogadores, pagamentos, eventos, despesas

### Fase 2 — Autenticação
- [ ] Tela de login (Spring Security + JWT já no backend)
- [ ] Guard Angular (`CanActivate`) protegendo rotas admin
- [ ] Interceptor HTTP para incluir token nas requisições
- [ ] Leitura do `time_id` do token (fim do `TIME_ID` hardcoded)
- [ ] Registro de novos times (auto-onboarding)

### Fase 3 — Atleta como usuário
- [ ] Login do atleta (perfil ATLETA)
- [ ] Tela de convocação (confirmar/recusar presença)
- [ ] Histórico de pagamentos pessoal
- [ ] Notificações push (Firebase + `SwPush` do Angular)

### Fase 4 — Monetização
- [ ] Modelo de planos no backend (tabela `plano`)
- [ ] Integração Pix (Asaas ou Mercado Pago)
- [ ] Limite de features por plano (guard no backend)
- [ ] Dashboard financeiro do gestor SaaS

### Fase 5 — Campeonatos
- [ ] Entidade `Campeonato` com múltiplos times
- [ ] Tabela de classificação pública
- [ ] Resultado de jogos com placar
- [ ] Súmula digital (gols, cartões)
