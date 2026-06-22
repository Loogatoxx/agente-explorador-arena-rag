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
