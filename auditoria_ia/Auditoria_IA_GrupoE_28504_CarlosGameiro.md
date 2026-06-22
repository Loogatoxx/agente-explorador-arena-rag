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


