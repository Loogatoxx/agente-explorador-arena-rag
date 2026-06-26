# Auditoria de Uso de IA — Projeto Final de Programação III

| Campo | Valor |
|-------|-------|
| **Número do Grupo** | E |
| **Aluno** | Carlos Gameiro |
| **Número de Aluno** | 28504 |
| **Ferramenta de IA** | Claude (Claude Code) |

> Este documento regista, de forma individual e transparente, as interações
> com a IA durante o desenvolvimento do projeto. Para cada interação fica
> registado **o prompt** (a pergunta/contexto que dei à IA) e **a resposta da
> IA** (o que foi gerado ou decidido). Os prompts originais foram feitos em
> francês e estão aqui sintetizados em português.
>
> Objetivo pedagógico: demonstrar capacidade de fazer as perguntas certas e de
> compreender criticamente o código antes de o integrar no projeto.

---

## Interação #1 — Definição do método de trabalho e arranque (2026-06-22)

### O meu Prompt
Apresentei o projeto (criar a IA do robô da Arena 3D RAG, descrito no
enunciado em PDF) e defini as regras de trabalho:
- Todo o projeto em português; os prompts seriam em francês, com resposta em francês.
- A cada etapa concluída: fazer vários commits pequenos e progressivos (e push),
  porque o professor valoriza um histórico Git organizado.
- Manter um "bloco de notas" / diário que nunca é apagado, onde se vai
  escrevendo o histórico desde o início (dificuldades, alterações, abandonos),
  para depois conseguir explicar tudo ao professor.
- Registar a conversa e, sobretudo, os prompts, por serem relevantes para a
  avaliação da transparência no uso de IA.

### A Resposta da IA
- Leu e analisou as 18 páginas do enunciado.
- Verificou o ambiente (Git, Java 25, Maven/Ollama/`gh` em falta).
- Criou a estrutura inicial do projeto Maven com a dependência Gson 2.10.1.
- Criou o esqueleto POO das 5 classes obrigatórias, já separadas por pacotes
  (`rede`, `ia`, `modelo`, `ui`) para respeitar o SRP e evitar a "Classe Deus".
- Criou este sistema de auditoria e o `DIARIO_DE_BORDO.md`.
- Organizou o trabalho em 3 commits temáticos.
- Sinalizou que o *push* fica pendente até existir um repositório GitHub
  privado (com o professor `pscm@ipt.pt` convidado).

---

## Interação #2 — Configuração do GitHub e equipa (2026-06-22)

### O meu Prompt
Em resposta à pergunta sobre como criar o repositório, escolhi a opção de
**instalar a CLI `gh` e deixar a IA criar tudo** (repositório privado, remote,
push e convite do professor). Acrescentei que tenho um colega de grupo:
**Afonso Trindade, nº 28292**, e pedi para adaptar o projeto a isso.

### A Resposta da IA
- Confirmou que o Homebrew está disponível e iniciou a instalação da `gh`.
- Criou o ficheiro de auditoria individual do Afonso
  (`Auditoria_IA_GrupoXX_28292_AfonsoTrindade.md`), por o professor exigir um
  ficheiro por aluno.
- Registou o colega no diário de bordo e na memória do projeto.
- Ficou a faltar apenas a autenticação interativa na `gh` (passo único feito
  pelo próprio utilizador) para depois criar o repositório e fazer push.

---

## Interação #3 — Criação do repositório e primeiro push (2026-06-22)

### O meu Prompt
Confirmei que a autenticação na `gh` estava feita e autorizei a IA a criar o
repositório e fazer o push. Ofereci também acesso ao ecrã/controlo do
computador caso fosse necessário para alguma tarefa.

### A Resposta da IA
- Verificou o `gh auth status` (conta `Loogatoxx`, scope `repo`).
- Criou o repositório **privado** `agente-explorador-arena-rag`, configurou o
  remote `origin` e fez **push** dos 6 commits.
- Explicou que, para este projeto, não precisa de controlar o ecrã (tudo é
  feito por terminal), mas guardou a opção para depuração visual futura (ex.:
  janela Swing).
- Indicou que o convite do professor terá de ser feito pela web (o GitHub
  convida por nome de utilizador, não por email).

---

## Interação #4 — Convenção de autoria dos commits (2026-06-22)

### O meu Prompt
Propus uma convenção: no fim de cada prompt eu indico quem o pensou; se for só
eu, o commit fica no meu nome; se for só o Afonso, no nome dele; se formos os
dois, fica em coautoria. Perguntei à IA se era uma boa abordagem.

### A Resposta da IA
- Validou a convenção como boa e honesta (reflete quem pilotou cada parte, que
  é o que o professor quer avaliar).
- Acrescentou dois refinamentos: (1) aplicar a mesma regra aos ficheiros de
  auditoria individuais (o prompt vai para a auditoria de quem o pensou);
  (2) manter a atribuição fiel à realidade, sem inflacionar números.
- Explicou o detalhe técnico de `git commit --author` para os commits do Afonso
  e a manutenção do `Co-Authored-By: Claude` como prova de transparência.
- Registou a convenção no diário de bordo e aplicou-a já a este commit (pensado
  por mim, logo em meu nome).

---

## Interação #5 — Motor heurístico (opção A), trabalho conjunto (2026-06-22)

### O meu Prompt
Decidimos avançar com a opção A — o motor heurístico de navegação. Este trabalho
foi pensado em conjunto comigo e com o Afonso (logo, commit em coautoria). A meio
do processo, a revisão automática parou por limite de sessão e eu pedi para
continuar, confirmando que o commit ficaria nos dois nomes.

### A Resposta da IA
- Antes de codificar, correu um **workflow multi-agente de design** (3 propostas
  independentes + síntese) e escolheu uma arquitetura de **pipeline de avaliadores**
  (Strategy/Chain): Reflexos → Batalha → Atração → Mapa de Calor.
- Implementou as classes do pacote `heuristica` (Bussola, MemoriaHeuristica,
  ContextoDecisao, FiltroColisoes, interface AvaliadorAcao + 4 avaliadores) e
  transformou o `MotorHeuristico` numa fachada fina (SRP, anti "Classe Deus").
- Isolou a **incerteza dos eixos** num único booleano (`Bussola`) e o **estado
  mutável** numa única classe (`MemoriaHeuristica`).
- Escreveu **testes JUnit 5** (7 testes, todos verdes com `mvn test`).
- Quando a revisão adversarial em workflow foi cortada pela **limite de sessão**,
  fez a revisão **manualmente** e removeu código morto (`Bussola.oposta()`,
  `ContextoDecisao.direcoesBase()`).

---

## Interação #6 — Pipeline RAG (opção B) (2026-06-22)

### O meu Prompt
Pedi para avançar com a opção B — o pipeline RAG completo. (Prompt pensado por
mim, Carlos.)

### A Resposta da IA
- Instalou o **Ollama** (Homebrew) e descarregou os modelos `nomic-embed-text`
  e `qwen2.5-coder:0.5b-instruct-q4_K_M`, arrancando o servidor.
- Criou o pacote `ia`: `OllamaClient` (embeddings + geração ChatML com
  `raw=true` e temperatura 0), `Vetores` (semelhança de cossenos),
  `BaseConhecimento` (store vetorial em RAM) e `PipelineRag` (orquestra
  download → chunking → embeddings → retrieval → ChatML → unlock), mais o
  record `ResultadoRag`.
- Acrescentou `descarregarManual()` e `desbloquear()` ao `ArenaClient` e
  integrou o RAG no ciclo do `AgenteExplorador` (cofre → resolver → unlock →
  lista negra em falha).
- **Testou em direto** contra o Ollama (manual falso): o cosseno recuperou o
  chunk correto e o LLM devolveu `POLY-SEAL` limpo.
- Escreveu testes JUnit (cosseno, chunking, limpeza de chave, ChatML); o
  projeto tem agora **15 testes verdes**.
- Explicou a opção de `/api/generate raw=true` para enviar o ChatML construído
  em Java tal e qual (a técnica pedida no enunciado).

---

## Interação #7 — Interface gráfica (opção C), trabalho conjunto (2026-06-22)

### O meu Prompt
Em conjunto com o Afonso, decidimos avançar com a opção C — a interface gráfica
`PainelMapaCalor`. Como foi pensado pelos dois, o commit fica em coautoria.

### A Resposta da IA
- Implementou o `PainelMapaCalor` (Swing/Graphics2D) — a 5.ª classe obrigatória —
  com radar, mapa de calor, paredes/recursos/cofres/rivais, painel de telemetria
  e legenda.
- Manteve o SRP: a UI recebe os dados por `atualizar()` e lê só um snapshot
  imutável do mapa de calor; integrou-a no ciclo do `AgenteExplorador`.
- Para validar sem depender de ecrã, **renderizou o painel para um PNG**
  (`docs/dashboard.png`) e confirmou o resultado visualmente.
- Garantiu robustez *headless* (a janela é ignorada sem ecrã) e confirmou que os
  15 testes continuam verdes.

---

## Interação #8 — Otimização de desempenho ("modo ultracode") (2026-06-22)

### O meu Prompt
Pedi para otimizar tudo, em todas as categorias, para termos o robô mais
eficiente e ganhar aos outros grupos. Disse para usar o máximo de capacidade de
cálculo. (Prompt pensado por mim, Carlos.)

### A Resposta da IA
- Em vez de "afinar à toa", construiu um **simulador da Arena** para medir
  objetivamente o desempenho (labirinto, radar com oclusão, benchmark A/B).
- Implementou a maior melhoria (navegação): **mapa acumulado**, **pathfinding
  BFS**, **exploração por fronteiras** e **aprender com movimentos bloqueados**.
- O simulador apanhou um bug real (agente preso no limite do mapa), que foi
  corrigido — e a correção torna o agente robusto no jogo real.
- **Provou o ganho com números**: cenários resolvidos passaram de 6/30 para
  29/30 e o score global melhorou ~86,7% (e ~20% menos turnos nos cenários
  comuns). Travou o ganho num teste de regressão. 16 testes verdes.
- Depois otimizou as restantes categorias: **RAG** (retrieval top-K + limpeza
  robusta da chave) e **combate** (limiar de HP para não suicidar, margem de
  vantagem, retirada para curar). Ficou com **18 testes verdes**.

---

## Interação #9 — Torneio de estratégias (free-for-all) (2026-06-22)

### O meu Prompt
Pedi para continuar a performar: montar um torneio que testa muitas estratégias
e elege a mais robusta contra todas as outras, com pontuação precisa, simulador
**free-for-all multi-bots**, e um visual no fim. Ativei o "ultracode".

### A Resposta da IA
- (O workflow não estava ativo no contexto; a IA explicou que não o controla e
  fez tudo inline — o torneio corre em Java, grátis.)
- Acrescentou a dimensão **Postura** (caçar/neutro/evitar), construiu um
  **simulador Battle Royale free-for-all** e uma **liga** com pontuação por
  colocação, sobre muitos seeds (com variância e pior-caso).
- Descobriu um **paradoxo medido**: em mêlea, a passividade vence (atacar expõe a
  terceiros), ao contrário do 1v1. O **refinamento** elegeu **EVITAR + margem 30**
  (média 4.97, 16.7% vitórias, 0% eliminado 1.º), aplicada como novo default.
- Foi honesta quanto aos limites (simulação vs adversários reais) e travou o
  ganho num teste. 20 testes verdes. Produziu um gráfico comparativo.

---

## Interação #10 — Material de apoio: simulação visual + cheat sheet (2026-06-23)

### O meu Prompt
Pedi um "cheat code" em PDF para nos ajudar na defesa oral (garantir a nota) e
uma simulação visual do robô a mover-se na arena, como se estivéssemos à frente
dela, e ver os testes.

### A Resposta da IA
- Criou um **gravador de replay** e uma **animação HTML** (`docs/simulacao_robot.html`)
  que mostra o robô a explorar, apanhar recursos e abrir cofres (play/pausa,
  passo-a-passo, velocidade) — também mostrada inline.
- Gerou um **PDF de cheat sheet** (`docs/Cheatsheet_Oral_GrupoE.pdf`) com a
  arquitetura, o RAG, a heurística, o combate, os números e perguntas prováveis
  do professor com respostas.
- Sublinhou que isto é apoio à oral, mas que cada elemento deve mesmo
  compreender o código (a transparência conta).

---

## Interação #11 — "O robô falha os cofres" — diagnóstico e correção (2026-06-25)

### O meu Prompt
Disse para corrigir o facto de o robô falhar os cofres — suspeitava que o
**Ollama** estivesse mal configurado ou fraco demais. Pedi também para me
explicar como se inicializa o bot e para testar em direto.

### A Resposta da IA
- **Refutou a minha hipótese com dados:** não era o Ollama. A recuperação
  vetorial acerta sempre a secção certa; o problema era outro.
- Encontrou e corrigiu **3 causas-raiz**: (1) dávamos 3 secções ao LLM pequeno
  e ele escolhia o código errado → passa a receber só a melhor + extração do
  código por regex (5/5 enigmas); (2) um `unlock` seguido de `move` sem pausa
  disparava o **anti-flood** do servidor (o unlock vinha a `null`) → uma só
  chamada de servidor por turno; (3) o servidor devolve corpo `null` no
  **sucesso** (com +HP) e nós líamos como falha → passou a ser reconhecido.
- Mostrou o comando de arranque do bot
  (`java ... pt.ipt.arena.AgenteExplorador <SALA> <ID>`).
- **Testou em direto** (sala 9B8860): **4 cofres abertos** com +HP. 28 testes
  verdes, commits progressivos e push.

### O que retive
- Não assumir a causa "óbvia" (Ollama) — medir primeiro. O modelo pequeno até
  chega; o que o atrapalhava era o contexto a mais e o ritmo dos pedidos.

---

## Interação #12 — Deslocamento aos saltos + relatório de testes (2026-06-25)

### O meu Prompt
Pedi para mudar a forma de andar (em vez de linha a linha, saltar para ver várias
linhas de uma vez), otimizar 1v1 e battle royale, perguntar a que distância o
robô vê e otimizar em função disso, e **não esquecer o relatório** dos testes
para não repetir erros (lembrei o guloso que fez 6/30).

### A Resposta da IA
- **Mediu a vista ao vivo:** o radar vê um **quadrado 7×7** (3 casas à volta).
- Mudou a exploração: marcar como "vistas" as casas do radar e ir à casa **não
  vista** mais próxima → o robô **salta** em vez de pisar tudo.
- **Benchmarkou o raio** (0 a 5) em 30 e 120 cartas para escolher sem cair na
  armadilha do guloso: raio **3** dá **30/30** (e 120/120) e é **~3,9× mais
  rápido** que antes. A/B: guloso **6/30 → 30/30**, −96,7% de turnos.
- Escreveu o **relatório** `docs/Relatorio_Testes_Navegacao.md` com os números e
  as lições (guloso 6/30; saltar demais volta a falhar; medir em muitos seeds).
- 30 testes verdes, commits progressivos + push.

### O que retive
- Otimizar a cobertura é faca de dois gumes: saltar **demais** volta a falhar
  cofres (o mesmo erro do guloso). Validar sempre **30/30 / 120/120** antes de
  aceitar mais velocidade, e ancorar o parâmetro na medição real (raio = 3).

---

## Interação #13 — Testes reais no servidor + browser autónomo (2026-06-25)

### O meu Prompt
Pedi para fazer TODOS os testes reais no servidor e, se criar salas à mão fosse
demorado, para automatizar (controlo do meu PC; abri o Opera; tentar outro
browser se o Safari bloqueasse).

### A Resposta da IA
- Explicou que nenhum browser é controlável por captura de ecrã (lê mas não
  escreve) e que a solução é a **extensão Claude in Chrome**. Guiou a instalação,
  e depois **pilotou o Chrome** para criar a sala (password do cenário → "GERAR
  CÓDIGO DE SALA") e dar START (com um reload para destravar o socket).
- Lançou o nosso agente otimizado e **a missão foi concluída AO VIVO: 4/4 cofres**
  (chaves XTR-99 e VALVULA-ALT) em ~25 s, com `🏁 MISSÃO CONCLUÍDA` no histórico
  oficial do servidor.
- Avisou que a password de treino só gera missões **individuais**; combate
  1v1/battle royale reais não dão para testar com ela (fica para simulação).

### O que retive
- A automação de browser precisa da extensão certa (não basta "ver" o ecrã).
- Validação ponta-a-ponta no servidor real é o teste que mais conta — e passou.

---

## Interação #14 — Otimização de combate (1v1 + battle royale) (2026-06-25)

### O meu Prompt
"go combat" — otimizar o combate em 1v1 e battle royale contra vários perfis,
incluindo bots random.

### A Resposta da IA
- Criou um comparador (`CombatePerfis`) com **random no campo** e mediu, com 150/250
  seeds: o **agente entregue é o melhor nas duas frentes** — battle royale
  (colocação 6.247/8, 39% de 1.ºs) e 1v1 (média 63.5% de vitórias).
- Tentou uma melhoria (gatar a esquiva) mas mediu que **piorava tudo** e
  **reverteu** — explicou que fugir de um rival único é kiting útil.
- Concluiu, com dados, que **não vale a pena** mudar: o combo já é o ótimo e
  perseguir o 1v1 contra agressores estragaria a dominância em battle royale (o
  modo real). Escreveu `docs/Relatorio_Testes_Combate.md` e travou com testes.

### O que retive
- "Já somos os melhores" também é um resultado — provado com medição. E mexer só
  por mexer (a tal intuição) costuma piorar; é preciso medir antes e depois.

---

## Interação #15 — Machine Learning autónomo (Q-Learning) local (2026-06-25)

### O meu Prompt
Pedi para passar a um nível superior: substituir as regras heurísticas e o
otimizador hill-climbing por **ML autónomo de verdade**, a treinar 100% local no
meu MacBook (sem gastar créditos API), com carta-branca no design e na matemática.

### A Resposta da IA
- Escolheu **Q-Learning tabular** (justificou vs NEAT): integra-se como avaliador
  na arquitetura, é local/leve e explicável; reutiliza os nossos simuladores.
- Desenhou estado (compacto), ações (4 direções) e a **recompensa** (Δhp +
  sobreviver, malus de morte) para "forçar a meta".
- Escreveu as classes (`EstadoQ`, `TabelaQ`, `AvaliadorQ`, `TreinadorQ`) + testes,
  e treinou localmente: o agente **aprende** (curva sobe), mas o **Q puro ainda
  não bate o combo** (que usa BFS). Foi **honesto** sobre isso e identificou o
  próximo passo (híbrido: Q só para o combate). Relatório + 36 testes verdes.

### O que retive
- ML não é magia: um agente que aprende do zero não bate logo um sistema
  afinado com pathfinding. O valor está no **sistema autónomo** (e no plano para
  o híbrido), medido com honestidade — não em prometer milagres.

---

## Interação #16 — Q-Learning híbrido (tática aprendida + BFS) (2026-06-26)

### O meu Prompt
Validei o diagnóstico (BFS vs Q cego) e pedi o **híbrido**: o ML escolhe só a
TÁTICA (postura), a navegação fica no BFS. Estado 100% combate, recompensa
"abutre" (kills grátis premiados, dano em mêlée penalizado). Bater o combo.

### A Resposta da IA
- Implementou `AvaliadorQHibrido` (postura → execução delegada às regras), estado
  de combate, recompensa abutre + **recompensa terminal por colocação**.
- Treinou localmente e mediu a progressão: **4.73 (Q puro) → 5.71 (híbrido)**, vs
  combo **5.99** — fechou ~80% da diferença, ficou ombro-a-ombro.
- Tentou um tuning mais agressivo, mediu que **piorava (5.05)** e **reverteu**.
- Foi honesto: o híbrido é competitivo mas ainda não passou o combo; deu o caminho.

### O que retive
- A boa engenharia de ML aqui foi **separar tática de movimento** (intuição
  forte) + **alinhar a recompensa com o objetivo** (colocação). E, de novo,
  afinar pesos "a olho" piora — só a medição decide.


