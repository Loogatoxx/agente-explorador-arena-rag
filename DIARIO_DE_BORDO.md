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

---

## 2026-06-22 — Etapa 7: Motor heurístico real (navegação inteligente)
*(Prompt pensado pelos DOIS → commits em coautoria Carlos + Afonso.)*

### O que foi feito
- Substituído o *placeholder* pelo verdadeiro `MotorHeuristico`, desenhado como
  **pipeline de avaliadores** (padrão Strategy/Chain) por prioridade:
  **Reflexos → Batalha → Atração → Mapa de Calor**.
- Classes novas (pacote `heuristica`):
  - `Bussola` (enum) — **único** ponto de verdade sobre os eixos; resolve a
    incerteza da orientação num só booleano (`NORTE_E_Y_NEGATIVO`).
  - `MemoriaHeuristica` — **único** dono do estado mutável (mapa de calor,
    lista negra de cofres, fila de reflexos), com operações "Tell, Don't Ask".
  - `ContextoDecisao` — DTO imutável por turno (pré-computa paredes/alvos/rivais).
  - `FiltroColisoes` — regra de colisões transversal, calculada 1x por turno.
  - `AvaliadorAcao` (interface) + `AvaliadorReflexos`, `AvaliadorBatalha`,
    `AvaliadorAtracao`, `AvaliadorMapaCalor`.
  - `MotorHeuristico` passa a fachada fina (orquestra a cadeia; nunca devolve
    `null`, nunca propaga exceção).
- **Testes JUnit 5** (`MotorHeuristicoTest`, 7 testes): colisões, atração,
  exploração sem círculos, fuga/ataque em batalha, sanidade null e cercado.
  `mvn test` → 7/7 verdes.

### Processo (uso de IA)
- Usou-se um **workflow multi-agente de design** (3 propostas independentes —
  MVP, robustez, arquitetura — + síntese) para escolher a arquitetura.
- Implementação a partir da síntese, seguida de **revisão**.

### Decisões e justificações
- Eixos centralizados na `Bussola`: confirmar a orientação numa sala real =
  inverter 1 booleano. A distância de Manhattan é invariante ao sinal.
- Estado só na `MemoriaHeuristica`, atualizado pelo motor (nunca pelo ciclo) →
  SRP, `AgenteExplorador` mantém-se fino.
- Desempate **determinista** (ordem NORTE, ESTE, SUL, OESTE) → reprodutível.
- Cercado → limpar lista negra 1x e recalcular; se persistir, ação segura.

### Dificuldades / Alterações / Abandonos
- A **revisão adversarial em workflow** foi interrompida pela **limite de sessão
  (créditos)** — fez-se a revisão **manualmente**. Resultado: removido código
  morto (`Bussola.oposta()`, `ContextoDecisao.direcoesBase()`); restante lógica
  confirmada por testes + leitura.
- Adicionou-se **JUnit 5 + maven-surefire** ao `pom.xml`.
- A orientação do eixo Y continua por confirmar com sala ativa, mas está
  isolada num único booleano na `Bussola`.

---

## 2026-06-22 — Etapa 8: Pipeline RAG (Ollama + embeddings + ChatML)
*(Prompt pensado pelo Carlos → commits em nome do Carlos.)*

### O que foi feito
- **Ollama** instalado (Homebrew) e a correr; modelos descarregados:
  `nomic-embed-text` (embeddings, 768-dim) e
  `qwen2.5-coder:0.5b-instruct-q4_K_M` (LLM).
- Classes novas (pacote `ia`):
  - `OllamaClient` — `gerarEmbedding()` (`/api/embeddings`) e `gerarTextoRaw()`
    (`/api/generate` com `raw=true`, temperatura 0.0).
  - `Vetores` — semelhança de cossenos (pesquisa semântica).
  - `BaseConhecimento` — store vetorial em RAM + `maisRelevante()`.
  - `PipelineRag` — orquestra ingestão (download + chunking + embeddings),
    retrieval (cosseno), geração (ChatML) e submissão (unlock).
  - `ResultadoRag` (record) — chave + contexto + resposta crua.
- `ArenaClient` ganhou `descarregarManual()` e `desbloquear(code, ragChunk, llmRaw)`.
- `AgenteExplorador` integra o RAG no ciclo: quando há cofre sob o agente,
  resolve via RAG e submete; em falha, marca o cofre na lista negra (o motor
  força fuga).

### Verificação
- **Teste em direto** contra o Ollama (manual falso): o cosseno recuperou o
  chunk certo e o LLM devolveu a chave **`POLY-SEAL`** limpa (sem alucinação).
- **Testes JUnit**: `VetoresTest` (4) + `PipelineRagTest` (4, chunking/limpeza/
  ChatML). Total do projeto: **15 testes verdes** (`mvn test`).

### Decisões e justificações
- **ChatML construído em Java** + `/api/generate raw=true`: envia-se o ChatML
  tal e qual (com `<|im_start|>system/user/assistant`), demonstrando a técnica
  pedida no enunciado e forçando o LLM a responder só com a chave.
- **Temperatura 0.0** → determinismo (anula a criatividade do modelo).
- Cosseno isolado em `Vetores`, store em `BaseConhecimento`, orquestração em
  `PipelineRag` → SRP; o `AgenteExplorador` apenas delega.

### Dificuldades / Alterações / Abandonos
- O nome real do modelo LLM tem o sufixo **`-q4_K_M`** (não o nome curto do
  enunciado); ajustado no `OllamaClient`.
- Embeddings via `/api/embeddings` (devolve `embedding`, 768-dim).
- Sem sala ativa, `indexarManual()`/`desbloquear()` só serão testáveis
  ponta-a-ponta com uma sala real; a lógica RAG foi validada localmente com um
  manual falso.

---

## 2026-06-22 — Etapa 9: Interface gráfica (PainelMapaCalor)
*(Prompt pensado pelos DOIS → commits em coautoria Carlos + Afonso.)*

### O que foi feito
- Implementado o `PainelMapaCalor` (Java Swing / Graphics2D), a 5.ª classe
  obrigatória — completa-se assim a estrutura mínima exigida.
- Desenha um **radar** centrado no agente (13×13 células) com: **mapa de calor**
  (intensidade de verde conforme as visitas), **paredes** (laranja), **recursos**
  (verde), **cofres** (amarelo), **rivais** (vermelho) e o **agente** (azul),
  mais um painel de **telemetria** (posição, barra de HP, contagens, células
  visitadas) e uma **legenda**.
- A UI recebe os dados de fora por `atualizar(Percecao, Map)` e lê apenas um
  **snapshot imutável** do mapa de calor (`MemoriaHeuristica.snapshotMapaCalor()`)
  — nunca toca no estado mutável do motor.
- `AgenteExplorador` abre a janela (`PainelMapaCalor.abrirJanela`) e atualiza-a
  a cada turno; em ambiente *headless* a janela é simplesmente ignorada.

### Verificação
- Renderizou-se o painel para um **PNG** (estado de jogo de exemplo) e
  confirmou-se o resultado visualmente — ver `docs/dashboard.png`.
- `mvn test` continua com **15 testes verdes** (a UI não introduziu regressões).

### Decisões e justificações
- A janela é criada via método estático `abrirJanela` que devolve `null` em
  *headless* → o agente pode correr sem ecrã (e os testes não dependem de UI).
- SRP mantido: a UI só desenha; recebe dados prontos e um snapshot imutável.

### Dificuldades / Alterações / Abandonos
- Nada abandonado. Para validar sem depender de um ecrã, optou-se por renderizar
  o painel diretamente para uma imagem (`BufferedImage`) em vez de abrir janela.

---

## 2026-06-22 — Etapa 10: Otimização da navegação (com benchmark)
*(Prompt pensado pelo Carlos — "modo ultracode": otimizar tudo para ganhar.)*

### O que foi feito
- **Mapa acumulado** (`MapaDescoberto`): junta paredes/recursos/cofres vistos ao
  longo dos turnos (deixa de "esquecer" alvos fora do radar de 4.5).
- **Pathfinding BFS** (`Pathfinder`): primeiro passo do caminho mais curto até ao
  alvo, contornando paredes conhecidas (otimista nas zonas por explorar).
- Novos avaliadores: `AvaliadorAlvo` (vai ao alvo alcançável mais próximo por
  caminho real, não por linha reta) e `AvaliadorExploracao` (vai à fronteira não
  visitada mais próxima — cobre o mapa sem oscilar).
- **Aprender com movimentos bloqueados**: se a posição não muda, a célula-destino
  é memorizada como parede (resolve os limites do mapa, que não vêm em
  `objetos_fixos`, e qualquer "bloqueado" do servidor). Exceções: cofres e rivais.
- Motor **otimizado por omissão**; mantida a `MotorHeuristico.classico()` para o
  benchmark A/B.
- **Simulador da Arena** (`SimuladorArena`: labirinto determinista, radar 4.5 com
  oclusão por linha de visão, bordo de paredes) + **benchmark** (`BenchmarkNavegacao`)
  e teste de regressão (`BenchmarkNavegacaoTest`).

### Resultado medido (30 labirintos 24×24, HP não-limitante)
| Métrica | Clássico (guloso) | Otimizado |
|---|---|---|
| Cenários resolvidos | **6/30** | **29/30** |
| Turnos médios (nos 6 que ambos resolvem) | 186,0 | **148,7** (~−20%) |
| Score global (menor = melhor) | 73 116 | **9 746** (**−86,7%**) |

### Decisões e justificações
- No benchmark de navegação, HP é não-limitante para **isolar** a eficiência de
  navegação da gestão de bateria (medições limpas).
- O simulador tem **bordo de paredes** (a arena real é fechada — Fig. 2).

### Dificuldades / Alterações / Abandonos
- Bug encontrado pelo simulador: o agente ficava **preso no limite do mapa**
  (o limite não vinha como parede). Resolvido com o "aprender com bloqueios" —
  uma melhoria que **também** torna o agente robusto no jogo real.

---

## 2026-06-22 — Etapa 11: Otimização do RAG e do combate
*(Continuação do "modo ultracode" — Carlos.)*

### O que foi feito
- **RAG — retrieval top-K**: passa a juntar os **3** chunks mais relevantes
  (`BaseConhecimento.maisRelevantes`) como contexto, em vez de só 1 — dá mais
  informação ao LLM e melhora a precisão quando a resposta não está toda num
  parágrafo.
- **RAG — limpeza de chave mais robusta**: `limparChave` remove agora prefixos
  verbosos ("A chave é…", "chave:", "key:", "resposta:") além de aspas e
  pontuação. Re-testado em direto contra o Ollama → continua a extrair
  `POLY-SEAL` limpo.
- **Combate mais inteligente** (`AvaliadorBatalha`):
  - **limiar mínimo de HP** (40): não investe se estiver fraco, mesmo contra um
    rival mais fraco (evita suicídios);
  - **margem de vantagem** (15 = um golpe): só ataca com vantagem clara;
  - **retirada para curar**: ao fugir, se conhecer um cofre alcançável e o
    caminho não o aproximar do rival, retira-se para lá (cura +100 HP).

### Verificação
- Total: **18 testes verdes** (`mvn test`), incluindo os novos testes de limpeza
  de chave e de não-suicídio em combate.

### Decisões e justificações
- Top-K = 3 (equilíbrio entre contexto e ruído).
- A retirada para cura usa o mesmo `Pathfinder` da navegação (reutilização).

---

## 2026-06-22 — Etapa 12: Benchmark de duelos e calibração do combate
*(Continuação do "modo ultracode" — Carlos.)*

### O que foi feito
- **Simulador de duelos 1v1** (`SimuladorDuelo`): dois motores enfrentam-se numa
  pequena arena (com curas), permitindo um **sweep** de parâmetros de combate
  (limiar de HP × margem de vantagem) contra um adversário sempre-agressivo.
- Parâmetros de combate tornados configuráveis (`AvaliadorBatalha(limiar, margem)`
  + `MotorHeuristico.comCombate(...)`).

### Descoberta (medida) e decisão
- O combate **conservador** anterior (limiar=40, margem=15) **perdia muito**:
  **29 derrotas** em 40 duelos. A melhor fórmula (score = derrotas, mais perto de
  0) é **agressiva**: **limiar=0, margem=−15 → 4 derrotas, 14 vitórias, 22 empates**.
- **Porquê**: a igual velocidade, **fugir só encaixa golpes** (−15 sem ripostar),
  enquanto lutar troca 15-15. Só se foge se o rival estiver claramente mais forte
  (15+ HP acima), caso em que a retirada para cura ainda se aplica.
- Adotou-se o novo **default agressivo**; o teste de combate foi ajustado para a
  nova (e melhor) política. **18 testes verdes**.

### Limitações (honestidade)
- O sweep é contra **um** tipo de adversário (agressor puro) e num simulador
  simplificado; é indicativo, não definitivo — mas a razão mecânica generaliza.

---

## 2026-06-22 — Etapa 13: Torneio free-for-all e estratégia eleita
*(Continuação do "modo ultracode" — Carlos; o workflow não estava ativo, feito inline.)*

### O que foi feito
- Nova **dimensão estratégica**: `Postura` (CACAR / NEUTRO / EVITAR) via
  `AvaliadorPostura` — movimento proativo perante rivais visíveis (não adjacentes).
- **Simulador Battle Royale free-for-all** (`SimuladorBatalha`): vários bots em
  simultâneo, com perceção mútua, combate e curas; e uma **liga** que pontua cada
  estratégia pela colocação (último vivo = melhor), sobre muitos seeds.
- **Refinamento** (`RefinamentoBatalha`): 18 variantes avaliadas contra um campo
  fixo de 8 arquétipos.

### Descoberta (medida) — o PARADOXO 1v1 vs mêlea
- Na **liga de 8 bots**, a **passividade vence em média**: Pacifista 4.17 (melhor),
  enquanto os agressivos ficam por baixo (Agressor 2.80; eliminado 1.º em 23%).
  Razão: em mêlea, **atacar = ficar exposto a dano de terceiros**. É o **oposto**
  do 1v1 (Etapa 12), onde a agressão vencia.
- **Refinamento → estratégia eleita**: **postura EVITAR + margem 30**
  (limiar irrelevante): **média 4.97, 16.7% de vitórias, 0% eliminado 1.º**.
  Vence **o dobro** do pacifista puro (7%) E mantém heurística de combate real
  (elimina os claramente mais fracos).

### Decisão
- Adotada como **novo default** do agente (`MotorHeuristico` + `AvaliadorBatalha`
  passam a (limiar 0, margem 30) + `AvaliadorPostura(EVITAR)`). Teste de regressão
  `SimuladorBatalhaTest` trava o ganho. **20 testes verdes**.
- Modos diferentes → defaults diferentes; como o exame de combate é **multi-bot**,
  escolheu-se o vencedor do **free-for-all**. (O 1v1 fica documentado para contexto.)

### Limitações (honestidade)
- O torneio é contra os **nossos** arquétipos, num simulador simplificado: é uma
  boa aproximação do "meta", não a certeza do que farão os colegas.

---

## 2026-06-23 — Etapa 14: Material de apoio (visual + cheat sheet)
*(Prompt pensado pelo Carlos.)*

### O que foi feito
- **Gravador de replay** (`SimuladorArena.gravarReplayJson` + `GravadorReplay`):
  corre o agente otimizado e exporta a partida em JSON.
- **Simulação visual animada** do robô: `docs/simulacao_robot.html` (abre no
  browser — ver o robô a explorar, apanhar recursos e abrir os cofres, com
  play/pausa, passo-a-passo e velocidade). Também mostrada inline na conversa.
- **Cheat sheet para a defesa oral**: `docs/Cheatsheet_Oral_GrupoE.pdf` —
  arquitetura, RAG, heurística, combate, números e um bloco de **perguntas
  prováveis do professor com respostas**.

### Notas
- Material de apoio à apresentação; não altera o agente. Os 20 testes continuam
  verdes.

---

## 2026-06-23 — Etapa 15: Pontuação da evolução (todas as versões)
*(Prompt pensado pelo Carlos.)*

### O que foi feito
- `BenchmarkEvolucao`: pontua cada versão da NAVEGAÇÃO no mesmo conjunto de 30
  labirintos (score = turnos; menor = melhor), de forma determinista.

### Resultados (navegação)
| Versão | Cenários resolvidos | Score (menor = melhor) |
|---|---|---|
| V0 — placeholder (só MOVER_NORTE) | 0/30 | 90 000 |
| V1 — clássico (guloso, só radar) | 6/30 | 73 116 |
| V2 — otimizado (mapa + BFS + fronteiras) | **29/30** | **9 746** |

- Estável e determinista (re-execução dá o mesmo). O diagnóstico do único
  cenário-limite (seed 7) mostra que o agente **chega a todos os cofres**
  (cofresRestantes=0) — ou seja, V2 é **capaz** de 30/30; é um caso no limite do
  orçamento de turnos.

### Os 8 arquétipos de combate (versões tentadas, não todas mantidas)
- Já pontuados na Etapa 13 (liga free-for-all): colocação média de 2.80
  (Agressor) a 4.17 (Pacifista); a estratégia **eleita** (Evitar + margem 30)
  fica em **4.97**.

### "Algoritmo perfeito" — a forma de o obter
- O método é o **ciclo simulador-driven**: medir (benchmark) → ajustar →
  re-medir → travar com teste de regressão. Foi assim que passámos de 0/30 para
  29/30 e que elegemos a estratégia de combate. As "máximas hipóteses de 1.º
  lugar" = navegação V2 (quase perfeita) + combate eleito por torneio.
- **20 testes verdes** em todas as versões mantidas.

---

## 2026-06-23 — Etapa 16: Sobrevivência, navegação 30/30 e otimizador local
*(Prompt pensado pelo Carlos: garantir 30/30 e maximizar o combate.)*

### Descoberta importante (honestidade)
- A "HP não-limitante" do benchmark era enganadora: `Math.min(250, hp+cura)`
  faz o HP de 1.000.000 cair para 250 na primeira cura → o benchmark mede, de
  facto, com **HP realista (~250)**.
- Logo: **navegação PURA = 30/30** (o agente é capaz de alcançar todos os
  cofres, provado por uma corrida sem morte); **com HP realista = 29/30** — no
  seed 7 o agente **morre por esgotamento** durante uma exploração longa.
- **100% de sobrevivência NÃO é matematicamente garantível**: o HP é finito e a
  colocação das curas pode tornar impossível alcançar tudo a tempo. Maximiza-se,
  não se garante.

### O que foi feito
- `AvaliadorSobrevivencia`: quando o HP < 70, traça rota para a cura conhecida
  mais próxima (cofre +100 preferido; recurso +20). Integrado no motor por
  omissão e no `comEstrategia`. (Melhora a robustez no jogo real.)
- `OtimizadorLocal`: ferramenta de força bruta para correr **no PC do aluno**
  (milhares de simulações de combate) que escreve um **relatório JSON** com a
  melhor estratégia. Confirma (amostra pequena) que a estratégia atual
  **EVITAR + margem 30** é a melhor do varrimento (média ~5.75/7 vs campo).

### Conclusão
- **Navegação**: algoritmo perfeito (30/30 em capacidade). **Combate**: estratégia
  já no ótimo do espaço testado. Para afinar mais, correr o `OtimizadorLocal`
  com muitos seeds e enviar o JSON. **20 testes verdes.**

---

## 2026-06-23 — Etapa 17: ML automático, levier "remate" e o viés do vencedor
*(Prompt pensado pelo Carlos: passar dos 4.9, e ML auto-ajustável.)*

### O que foi feito
- **Levier "remate"** (`AvaliadorBatalha` 3 args): abate de graça um rival
  adjacente que morre num só golpe (<=15 HP) — kills sem risco.
- **`OtimizadorAuto`**: otimizador **automático** (hill-climbing com reinícios
  aleatórios), SEM grelha predefinida — parte de configs, muta o genoma
  (limiar/margem/postura/remate), sobe sempre para o melhor vizinho e recomeça.
  **Auto-ajusta-se** (a "ML local" pedida).

### Lição honesta — o VIÉS DO VENCEDOR
- A mesma config deu médias diferentes consoante a amostra: **8 seeds → 5.75**,
  **80 seeds → 5.125**, **200 seeds → 4.985**. Quando se escolhe o "melhor" de
  várias estimativas ruidosas, **sobrestima-se** (winner's curse). A verdade
  robusta (200 seeds) é **~4.985**. O 5.75 que citei antes era ruído — assumido.

### Decisão
- Novo default ("abutre"): **EVITAR + margem 90 (quase nunca inicia) + remate**.
  Média **4.985/8** vs o campo (era 4.750) → **+5%**. Muito perto de 5, mas não
  robustamente acima — é, ao que parece, o teto deste campo/métrica.
- **20 testes verdes** (teste de combate atualizado para o "remate").
