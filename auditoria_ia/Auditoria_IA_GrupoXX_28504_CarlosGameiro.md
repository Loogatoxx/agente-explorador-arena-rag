# Auditoria de Uso de IA — Projeto Final de Programação III

| Campo | Valor |
|-------|-------|
| **Número do Grupo** | (a confirmar) |
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


