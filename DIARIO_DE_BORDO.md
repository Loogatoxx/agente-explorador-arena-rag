# Diário de Bordo — Agente Explorador na Arena 3D RAG

> Documento histórico do desenvolvimento. **Nunca é apagado.** Cada etapa
> acrescenta informação no fim, formando um registo cronológico completo do
> percurso: decisões tomadas, dificuldades encontradas, coisas que tivemos de
> alterar e ideias que foram abandonadas.
>
> Objetivo: ter rasto suficiente para conseguir explicar ao professor, do
> princípio ao fim, como o projeto foi construído e que papel a IA teve nesse
> processo.

## Como ler este documento
- Cada entrada tem **data** e um **título de etapa**.
- Dentro de cada entrada há secções fixas:
  - **O que foi feito**
  - **Decisões e justificações**
  - **Dificuldades / Alterações / Abandonos**
- O detalhe das interações com a IA (os *prompts*) é registado em
  `auditoria_ia/` (entrega individual e avaliada à parte).

---

## 2026-06-22 — Etapa 0: Arranque e estrutura inicial

### O que foi feito
- Leitura e análise integral do enunciado (`enunciado_projeto_programacao_III_v.1.0.pdf`, 18 páginas).
- Levantamento do ambiente da máquina de desenvolvimento:
  - Git instalado e configurado (Carlos Gameiro / Aluno28504@ipt.pt).
  - **Java 25** instalado (cumpre o requisito "Java 11 ou superior").
  - **Maven** ainda não instalado na linha de comandos (o IntelliJ gere o Maven via `pom.xml`).
  - **Ollama** ainda não instalado (só é necessário quando se desenvolver a parte de IA/RAG).
- Criação da estrutura base do projeto:
  - `pom.xml` (Maven) com a dependência **Gson 2.10.1** exigida no enunciado.
  - `.gitignore` para Java / Maven / IntelliJ / macOS.
  - `README.md` com a descrição do projeto e da arquitetura.
- Criação do esqueleto POO com as **5 classes obrigatórias**, já separadas por
  responsabilidade (pacotes) para reforçar o SRP e evitar a "Classe Deus":
  - `pt.ipt.arena.AgenteExplorador` (Main / orquestrador)
  - `pt.ipt.arena.rede.ArenaClient` (HTTP com a Arena)
  - `pt.ipt.arena.ia.OllamaClient` (motor de IA local)
  - `pt.ipt.arena.modelo.DocumentoVetorial` (modelo de dados)
  - `pt.ipt.arena.ui.PainelMapaCalor` (interface Swing)
- Criação deste diário de bordo e do sistema de auditoria de IA (`auditoria_ia/`).

### Decisões e justificações
- **Java 17 como alvo de compilação** (`maven.compiler.release=17`): é LTS,
  está bem acima do mínimo (Java 11), suporta sintaxe moderna e é compatível
  com o JDK 25 instalado.
- **Pacotes por responsabilidade** (`rede`, `ia`, `modelo`, `ui`): a avaliação
  penaliza fortemente o anti-padrão "Classe Deus"; separar desde o início
  torna o SRP explícito.
- **Idioma**: todo o conteúdo do projeto (código, comentários, documentação)
  fica em **português**. Os *prompts* à IA são feitos em francês (preferência
  do autor) mas são traduzidos/sintetizados para português na auditoria, para
  manter o projeto coerente.
- **Disciplina de commits**: o enunciado valoriza commits progressivos e
  penaliza o "commit gigante" único. Adotámos a regra de fazer vários commits
  pequenos e temáticos por cada etapa de trabalho.

### Dificuldades / Alterações / Abandonos
- **Push para o GitHub ainda bloqueado**: a CLI `gh` não está instalada e ainda
  não existe repositório remoto configurado. Por agora os commits são apenas
  locais; falta decidir como criar o repositório privado e convidar o professor
  (`pscm@ipt.pt`). A resolver na próxima etapa.
- Nada foi abandonado nesta etapa (é o arranque).

---

## 2026-06-22 — Etapa 1: Equipa e preparação do GitHub

### O que foi feito
- Registado o **2º elemento do grupo**: Afonso Trindade (nº 28292).
- Criado o ficheiro de auditoria individual do Afonso em `auditoria_ia/`
  (cada aluno entrega o seu próprio ficheiro).
- Instalada a CLI **`gh`** (GitHub CLI) via Homebrew, para criar o repositório
  privado, configurar o remote e convidar o professor.

### Decisões e justificações
- Escolhida a abordagem "instalar `gh` e automatizar" para a criação do
  repositório e o convite do professor (`pscm@ipt.pt`), em vez de o fazer
  manualmente no site.
- Mantém-se o número do grupo como "(a confirmar)" nos nomes dos ficheiros de
  auditoria (`GrupoXX`) até ser indicado.

### Dificuldades / Alterações / Abandonos
- A autenticação na `gh` (`gh auth login`) é **interativa** e tem de ser feita
  uma única vez pelo próprio utilizador no seu terminal; depois disso os
  comandos `gh` passam a funcionar de forma automática.

---

## 2026-06-22 — Etapa 2: Repositório remoto criado e primeiro push

### O que foi feito
- Autenticação na `gh` concluída (conta GitHub: `Loogatoxx`).
- Criado o repositório **privado** `agente-explorador-arena-rag`:
  https://github.com/Loogatoxx/agente-explorador-arena-rag
- Configurado o remote `origin` e feito **push** dos 6 commits iniciais.
- A branch `main` local passa a seguir `origin/main`.

### Decisões e justificações
- Repositório **privado**, como exige o enunciado.
- Nome do repositório: `agente-explorador-arena-rag` (descritivo).

### Dificuldades / Alterações / Abandonos
- **Convite do professor (`pscm@ipt.pt`)**: a CLI `gh` / API do GitHub adiciona
  colaboradores por **nome de utilizador**, não por email. Como só temos o email
  do professor, o convite tem de ser feito pela interface web do GitHub
  (Settings → Collaborators → Add people → escrever o email). Fica como passo a
  realizar.
- **Número do grupo** ainda por confirmar (os ficheiros de auditoria usam
  `GrupoXX`).

---

## 2026-06-22 — Etapa 3: Grupo E confirmado e questão da coautoria

### O que foi feito
- Confirmado que somos o **Grupo E**.
- Ficheiros de auditoria renomeados de `GrupoXX` para `GrupoE`
  (`Auditoria_IA_GrupoE_28504_CarlosGameiro.md` e
  `Auditoria_IA_GrupoE_28292_AfonsoTrindade.md`), com o campo "Número do Grupo"
  atualizado para E.
- Confirmado que o Afonso **aceitou o convite** e já é colaborador do
  repositório (utilizador GitHub: `Afonso4538`, id 279673620).

### Decisões e justificações
- Levantou-se a questão de como fazer os commits aparecerem em **nome dos dois**
  elementos (à semelhança do trailer `Co-Authored-By: Claude`).
  - O GitHub liga um coautor ao seu perfil através do email; o email "noreply"
    do Afonso é `279673620+Afonso4538@users.noreply.github.com`.
  - Princípio adotado: a coautoria (`Co-Authored-By:`) só faz sentido em
    trabalho **realmente feito em conjunto**; o trabalho individual de cada um
    deve ser commitado pelo próprio (autoria autêntica), porque o professor
    avalia "quem programou o quê". A forma concreta fica a combinar com o Afonso.

### Dificuldades / Alterações / Abandonos
- Nada abandonado nesta etapa.

---

## 2026-06-22 — Etapa 4: Convenção de autoria dos commits e da auditoria

### O que foi feito
- Definida e adotada uma **convenção de autoria** para todo o projeto.

### Decisões e justificações
- No fim de cada prompt, o autor indica **quem o pensou**: "Carlos", "Afonso"
  ou "os dois". A partir disso:
  - **Carlos sozinho** → commit em nome do Carlos.
  - **Afonso sozinho** → commit em nome do Afonso (via `git commit --author`
    com o email GitHub do Afonso: `279673620+Afonso4538@users.noreply.github.com`).
  - **Os dois** → o commit leva o outro elemento em `Co-Authored-By:`.
  - Em **todos** os casos mantém-se `Co-Authored-By: Claude` (transparência no
    uso de IA, critério avaliado).
- **Refinamento 1**: a mesma regra aplica-se ao ficheiro de **auditoria de IA** —
  o prompt é registado na auditoria de quem o pensou (a auditoria é individual).
- **Refinamento 2**: a atribuição tem de **refletir a realidade** (quem
  concebeu de facto o prompt), e não servir para equilibrar números. É a
  contribuição autêntica que o professor avalia.
- Identidades GitHub: Carlos = `Loogatoxx` (id 279675221);
  Afonso = `Afonso4538` (id 279673620).

### Dificuldades / Alterações / Abandonos
- Nada abandonado. (Em alternativa mais "pura", o Afonso poderia commitar
  sempre a partir da sua própria máquina; optou-se pela atribuição por `--author`
  por praticidade, mantendo a autenticidade.)

---

## 2026-06-22 — Etapa 5: ArenaClient (register + perceive)
*(Prompt pensado pelo Afonso → commits em nome do Afonso.)*

### O que foi feito
- Estudada a API real do servidor através do **OpenAPI** (`/openapi.json`),
  em vez de assumir os endpoints. Endpoints confirmados:
  - `POST /arena/{room_id}/register?robot_id=...` (resposta `{status, message}`)
  - `GET  /arena/{room_id}/perceive/{robot_id}`
  - `POST /arena/action` (body `{room_id, robot_id, action}`)
  - `POST /arena/{room_id}/unlock?robot_id=&code=&rag_chunk=&llm_raw=`
  - `GET  /arena/{room_id}/download_manual`
- Implementada a classe `ArenaClient` com:
  - `registar()` → POST de registo;
  - `percecionar()` → GET de telemetria;
  - tratamento de rede robusto (timeouts + try-catch), devolvendo `null` em
    falha para o ciclo recuperar (requisito de resiliência do enunciado);
  - parsing com **Gson** para `JsonObject`.
- **Teste em direto** contra o servidor real: o cliente liga-se e faz parse
  corretamente. Compilação validada com a dependência Gson.

### Decisões e justificações
- `registar()`/`percecionar()` devolvem `JsonObject` (JSON estruturado) nesta
  fase; os **modelos tipados** da telemetria (classes POJO) ficam para a etapa
  seguinte, quando houver uma sala ativa para confirmar os nomes reais dos
  campos. Evita-se assim mapear campos "às cegas".
- Instalou-se o **Maven** (via Homebrew) para compilar/correr o projeto com as
  dependências a partir da linha de comandos.

### Dificuldades / Alterações / Abandonos
- **Sala de treino indisponível**: `aluno_treino_2026` devolve "Sala não existe
  ou já foi fechada" e `perceive` devolve `{"error":"Sala inválida"}`. O código
  está correto, mas falta uma **sala instanciada** para testes reais. Hipótese:
  este código funciona como *password de cenário* na consola web (Fig. 3 do
  enunciado), que gera um código de sala de 6 caracteres. A confirmar quando se
  quiser fazer testes com telemetria real.

---

## 2026-06-22 — Etapa 6: Modelos de telemetria + ciclo Sense-Think-Act
*(Prompt pensado pelo Afonso → commits em nome do Afonso.)*

### O que foi feito
- **Modelos de dados (POJO)** para mapear o JSON da telemetria com Gson:
  `Coordenada`, `EstadoRobo`, `Cofre`, `RoboRival` e o agregador `Percecao`.
- Enum `Acao` (MOVER_NORTE/SUL/ESTE/OESTE) com o comando exato do servidor.
- `MotorHeuristico` (pacote `heuristica`): classe de decisão com `decidir(Percecao)`
  — por agora um *placeholder*, para a lógica real vir depois.
- `ArenaClient` ganhou:
  - `agir(Acao)` / `agir(String)` → `POST /arena/action`;
  - `percecionarTipado()` → telemetria já convertida para `Percecao`.
- `AgenteExplorador`: implementado o **ciclo infinito Sense-Think-Act**, com
  tratamento de lobby (`game_started=false`), fim de jogo (`game_over`), falhas
  de rede (telemetria `null`), `try-catch` de segurança e **pausa anti-flood de
  400 ms** no fim de cada iteração.
- Compilação validada com `mvn compile` (BUILD SUCCESS, 12 classes).
- **Demonstração**: corrido contra a sala (inativa), o agente arranca, regista,
  entra em espera no lobby em ciclo e **não rebenta** — exatamente o pretendido.

### Decisões e justificações
- A **decisão** vive no `MotorHeuristico`, não no `AgenteExplorador`, para manter
  o orquestrador fino (anti "Classe Deus" / SRP).
- Enum `Acao` em vez de strings soltas → segurança de tipos.
- Pausa de **400 ms** (dentro da margem 350-400 ms recomendada contra o Anti-Flood).
- `percecionar()` (bruto, `JsonObject`) mantém-se a par de `percecionarTipado()`
  (modelo): o bruto serve para depuração/UI, o tipado para a lógica.

### Dificuldades / Alterações / Abandonos
- Os **nomes dos campos internos** dos modelos (ex.: `x`/`y`/`hp`) são inferidos
  do enunciado; ficam marcados nos `@SerializedName` para confirmação fácil
  quando houver uma sala ativa.
- O `MotorHeuristico` é, de propósito, um *placeholder* (devolve sempre
  MOVER_NORTE) até à etapa de heurística — a navegação inteligente (colisões,
  mapa de calor, atração por alvos, luta/fuga) ainda não foi escrita.
