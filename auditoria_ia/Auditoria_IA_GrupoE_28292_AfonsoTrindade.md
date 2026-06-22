# Auditoria de Uso de IA — Projeto Final de Programação III

| Campo | Valor |
|-------|-------|
| **Número do Grupo** | E |
| **Aluno** | Afonso Trindade |
| **Número de Aluno** | 28292 |
| **Ferramenta de IA** | Claude (Claude Code) |

> Este documento é **individual**. O professor exige que cada elemento do grupo
> entregue o seu próprio ficheiro com **as conversas que teve com a IA**, para
> avaliar separadamente a capacidade de raciocínio e de *prompt engineering* de
> cada aluno.
>
> Os prompts originais foram pensados em francês/português e estão aqui
> sintetizados em português.

---

## Interação #1 — Implementação do ArenaClient (register + perceive) (2026-06-22)

### O meu Prompt
Pedi para começar a parte de código pelo ponto 1 do plano: a classe
`ArenaClient` com os métodos de **registo** (`register`) e de **perceção**
(`perceive`). (Prompt pensado por mim, Afonso.)

### A Resposta da IA
- Antes de escrever código, foi buscar o **OpenAPI/Swagger** do servidor
  (`/openapi.json`) para obter os endpoints e parâmetros exatos, em vez de
  adivinhar. Endpoints confirmados:
  - `POST /arena/{room_id}/register?robot_id=...` (resposta `{status, message}`)
  - `GET /arena/{room_id}/perceive/{robot_id}`
- Implementou o `ArenaClient` com `registar()` e `percecionar()`, usando
  `java.net.http.HttpClient`, **timeouts** e **try-catch robustos** (IOException,
  InterruptedException, respostas inválidas) para o agente nunca rebentar —
  requisito de resiliência do enunciado. Em caso de falha devolve `null` para o
  ciclo voltar a tentar.
- Usou **Gson** (`JsonParser`) para converter a resposta em `JsonObject`.
- **Testou em direto** contra o servidor real: o cliente liga-se e faz parse
  corretamente. Descobriu-se que a sala `aluno_treino_2026` não está
  instanciada de momento (resposta `{"status":"erro", ...}` / `{"error":"Sala
  inválida"}`) — o código está correto, falta uma sala ativa para testes reais.

---

## Interação #2 — Modelos de telemetria + ciclo Sense-Think-Act (2026-06-22)

### O meu Prompt
Como o professor ainda não ativou a sala, pus essa parte em standby e pedi para
continuar para o passo mais recomendado a seguir desta etapa. (Prompt pensado
por mim, Afonso.)

### A Resposta da IA
- Criou os **modelos POJO** que mapeiam o JSON da telemetria com Gson
  (`Coordenada`, `EstadoRobo`, `Cofre`, `RoboRival`, `Percecao`) e o enum `Acao`.
- Criou o `MotorHeuristico` (decisão isolada do orquestrador, para respeitar o
  SRP e evitar a "Classe Deus") — por agora um *placeholder*.
- Completou o `ArenaClient` com `agir()` (`POST /arena/action`) e
  `percecionarTipado()`.
- Implementou o **ciclo Sense-Think-Act** no `AgenteExplorador`, com lobby,
  fim de jogo, falhas de rede, `try-catch` e pausa anti-flood de 400 ms.
- Compilou com Maven e demonstrou em execução que o agente arranca e fica em
  espera sem rebentar, mesmo com a sala inativa.
- Explicou que os nomes de campos internos são inferidos do enunciado e ficam
  fáceis de ajustar quando houver telemetria real.

---

## Interação #3 — Motor heurístico (opção A), trabalho conjunto (2026-06-22)

### O meu Prompt
Em conjunto com o Carlos, decidimos avançar com a opção A — o motor heurístico de
navegação. Como foi pensado pelos dois, o commit fica em coautoria (Carlos +
Afonso).

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
- A revisão adversarial em workflow foi cortada pela **limite de sessão**; a
  revisão foi feita manualmente, com remoção de código morto.

---

## Interação #4 — Interface gráfica (opção C), trabalho conjunto (2026-06-22)

### O meu Prompt
Em conjunto com o Carlos, decidimos avançar com a opção C — a interface gráfica
`PainelMapaCalor`. Como foi pensado pelos dois, o commit fica em coautoria.

### A Resposta da IA
- Implementou o `PainelMapaCalor` (Swing/Graphics2D) — a 5.ª classe obrigatória —
  com radar centrado no agente, mapa de calor das células visitadas, paredes,
  recursos, cofres, rivais, painel de telemetria e legenda.
- Manteve o SRP (a UI só desenha; recebe um snapshot imutável do mapa de calor)
  e integrou-a no ciclo do `AgenteExplorador`, com robustez *headless*.
- Validou o resultado renderizando o painel para um PNG (`docs/dashboard.png`),
  confirmando-o visualmente, sem regressões nos 15 testes.


