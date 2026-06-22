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

