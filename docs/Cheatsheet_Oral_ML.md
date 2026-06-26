# Cheat Sheet ORAL — Sense-Think-Act vs Machine Learning (Grupo E)

> Argumentos-chave para defender a nossa abordagem de forma profissional.
> Mensagem central: **escolhemos a melhor ferramenta para cada problema e
> medimos tudo com rigor científico.**

---

## 1. A arquitetura entregue: agente Sense-Think-Act (heurística)

Ciclo a cada turno: **Percecionar → Decidir → Agir**, sem "Classe Deus".

- **Decisão = pipeline de avaliadores por prioridade** (`AvaliadorAcao`, padrão
  Strategy/Chain): Reflexos → Batalha → Aspirador → Esquiva → Alvo → Canto →
  Exploração. O 1.º que decide ganha. Cada avaliador tem **uma só
  responsabilidade** (SRP).
- **Inteligência espacial:** mapa acumulado + **pathfinding BFS** (caminho mais
  curto a contornar paredes) + **exploração ciente do radar** (avança aos saltos:
  marca como "visto" o quadrado do radar → ~4× mais rápido, 30/30 cofres).
- **Combate "abutre":** sobrevive à mêlée e fica com os abates de graça (remate).
- **Resolução de cofres = RAG local** (Ollama): manual → chunks → embeddings →
  recuperação por cosseno → LLM extrai a chave. 100% local.

**Números:** navegação **30/30**; battle royale **≈6.14/8** (melhor do campo);
cofres abertos AO VIVO no servidor real.

---

## 2. Fomos mais longe: construímos um sistema de ML autónomo (Q-Learning)

Não nos contentámos com a heurística — implementámos **Reinforcement Learning de
verdade**, 100% local (sem gastar API), a treinar por self-play nos nossos
simuladores rápidos.

- **Q-Learning tabular** (escolhido vs NEAT: integra-se como avaliador, é leve e
  **explicável**). Equação de Bellman, ε-greedy, tabela em memória.
- **Híbrido (a chave):** o ML decide a **TÁTICA** (postura: Agressividade /
  Esquiva / Recuo-cura / Ignorar) e a **execução** fica no BFS já afinado. =
  melhor dos dois mundos.
- **Engenharia séria:** estado de combate enriquecido, recompensa parametrizável,
  **Grid Search multithreaded** (`parallelStream`, todos os núcleos) e avaliação
  paralela.

---

## 3. O resultado HONESTO (e porque é uma força, não uma fraqueza)

Progressão medida: Q puro **4.73** → híbrido **5.48** → +recompensa de colocação
**5.71** → V3 com visão enriquecida (treino 18k) **5.82/8** (a 500 seeds).

**O combo (heurística) continua campeão (~6.06), +0.25 à frente.** E sabemos
explicar PORQUÊ:

> Uma heurística perita com **BFS** já incorpora conhecimento ótimo (caminho mais
> curto, prioridade a curas, evasão). Um **RL tabular** tem de *descobrir* tudo a
> partir de uma perceção comprimida e de uma tabela finita, com **tempo de treino
> limitado**. Por isso é tão difícil de bater — e igualar já é notável.

**A lição científica (o nosso melhor argumento):** o Grid Search a **80 seeds**
deu 6.138 (parecia vitória!). Re-medimos a **500 seeds** → **5.66**. Era o
**viés do vencedor** (overfitting à amostra pequena). **Tivemos o reflexo de
verificar** — e corrigimos. *É isto que distingue engenharia de ML a sério de
"deu um número bom".*

---

## 4. A decisão de engenharia

- **Torneio → combo heurístico** (robusto, determinista, garante a 1.ª posição).
- **ML → prova de excelência** no relatório (`docs/Relatorio_ML_Hibrido.md`).
- Princípio: **a ferramenta certa para o problema certo**, validado por medição.

---

## 5. Perguntas prováveis do professor → respostas

- **"Porque não usaram só ML?"** → Porque medimos: o combo com BFS é mais forte e
  100% fiável; o RL tabular não o supera em tempo de treino realista. Escolhemos
  por dados, não por moda.
- **"O ML não serviu para nada?"** → Serviu de prova de domínio (RL, recompensas,
  estado, paralelismo) e de validação independente do combo. E o híbrido
  chegou ao nível do combo — confirma que o combo está perto do ótimo.
- **"Como sabem que o combo é o melhor?"** → Benchmarks A/B multi-seed contra um
  campo de 8 perfis + random (centenas de partidas), com cuidado anti-viés-do-vencedor.
- **"E a parte de IA generativa?"** → RAG local com Ollama resolve os cofres
  (recuperação + LLM), validado ao vivo no servidor.

> **Soundbite final:** "Construímos os dois — uma heurística perita e um agente de
> RL — e deixámos os **dados** decidir qual entra na arena. O combo ganha; o ML
> prova que entendemos o problema a fundo."
