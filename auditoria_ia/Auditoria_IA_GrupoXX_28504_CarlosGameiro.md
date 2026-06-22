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
