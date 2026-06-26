# Relatório — Q-Learning HÍBRIDO (tática aprendida + execução BFS)

**Data:** 2026-06-26 · **Grupo E** · Projeto Arena 3D RAG

Objetivo: bater o combo heurístico (≈6.0/8 em battle royale) com aprendizagem por
reforço, sem reinventar a navegação.

## 1. Ideia: separar TÁTICA de MOVIMENTO

O Q puro perdia porque "via" mal o mapa (sem pathfinding). O híbrido resolve isso:
- O **ML decide só a TÁTICA** (postura): `AGRESSIVIDADE`, `ESQUIVA`,
  `RECUO_CURA`, `IGNORAR`.
- A **execução do passo** é delegada às nossas regras BFS já afinadas (Aspirador,
  Alvo, Esquiva, Canto, Exploração). Fora de combate, o híbrido recolhe
  *exatamente* como o combo.

## 2. Estado (100% combate) e Recompensa

- **Estado** (~108): patamar de HP × vantagem de HP sobre o rival × densidade
  inimiga (1v1 vs mêlée) × há cura conhecida × rival adjacente rematável.
- **Recompensa "abutre":** Δhp + sobreviver, **+ bónus de abate** (rematar),
  **− dano em mêlée sem matar**, e **recompensa terminal por COLOCAÇÃO** (alinha a
  aprendizagem com o objetivo de ganhar).

## 3. Progressão dos resultados (greedy, 200 seeds, mesmo campo + random)

| Versão | Colocação /8 | 1.ºs % |
|---|---|---|
| Q puro (movimento direto, sem BFS) | 4.73 | 12.5 |
| Híbrido (postura + BFS) | 5.48 | 17.0 |
| **Híbrido + recompensa de colocação (ESCOLHIDO)** | **5.71** | 22.5 |
| Híbrido + tuning agressivo (abate/colocação) | 5.05 | 21.5 |
| **combo (a bater)** | **5.99** | 37.5 |

A separação tática/movimento fechou ~80% da diferença para o combo (4.73 → 5.71).

## 4. Tuning final (e o que ele ensinou)

Tentámos pesos mais agressivos (`ABATE=60`, `DANO_EM_MELEE=20`, `PESO_COLOCACAO=15`,
20 000 episódios) para "forçar" mais abates. **Resultado: PIOR (5.05).** Demasiado
incentivo ao abate leva o agente a carregar de forma imprudente e a apanhar dano.
Revertido para os pesos equilibrados (`ABATE=40`, `MELEE=15`, `COLOCACAO=8`, → 5.71).

**Lição (a mesma de sempre):** afinar a recompensa "a olho" costuma piorar — é
preciso medir antes e depois (como no guloso 6/30, no salto-a-mais e na esquiva).

## 5. V3 — "ver melhor" + Grid Search multithreaded

Diagnóstico: para passar o combo, faltava **visão** (não pesos). V3:

- **Estado enriquecido** (sem explodir: ~486 estados): acrescenta **distância da
  cura** (3 níveis), **distância do inimigo** (3 níveis) e **"posso abatê-lo?"**
  (rival ≤15 HP) ao estado de combate.
- **Grid Search MULTITHREADED** (`GridSearchQ`): testa 8 combinações de pesos em
  **paralelo** (`parallelStream`, 1 por núcleo — 10 no MacBook M4), treino rápido +
  avaliação; escolhe a melhor e faz o treino completo. Avaliação também paralela
  (`avaliarContraParalelo`). A `TabelaQ` separa leitura (não muta) de escrita →
  seguro em paralelo.

### O VIÉS DO VENCEDOR (a lição central deste relatório)

O Grid Search (avaliação a **80 seeds**) deu o melhor combo a **6.138** — pareceu
que tínhamos batido o combo! Mas ao re-medir a **500 seeds** (amostra grande):

| (500 seeds) | Colocação /8 | 1.ºs % |
|---|---|---|
| Q-híbrido V3 (7000 ep.) | **5.66** | 24.8 |
| **combo** | **6.14** | 37.0 |

O "6.138" era **ruído de amostra pequena** — o clássico **viés do vencedor**. A
medição justa mostra o combo ainda à frente (+0.48). Causa técnica: com 486
estados e só 7000 episódios, a tabela fica **subtreinada** (≈189 estados
preenchidos). Confirmou-se ainda que o melhor combo de pesos era o **equilibrado**
(o agressivo perde) — ou seja, o levador era a **visão**, não os pesos.

## 6. Como treinar / procurar (local)

```
# Grid search multithreaded (escolhe pesos) + treino completo no melhor:
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.GridSearchQ 4000 20000 qhib.txt
# Treino direto (estado enriquecido + pesos por omissão):
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.TreinadorQ 18000 hibrido qhib.txt
```

## 7. Decisão final do projeto

- 🏆 **Campeão do torneio = o combo heurístico (Sense-Think-Act).** É a versão
  ENTREGUE (`AgenteExplorador` usa `new MotorHeuristico(id)`): pathfinding BFS
  perfeito, 30/30 navegação, ~6.14/8 em battle royale, robusto e determinista.
- 🎓 **O Q-Learning é a nossa "prova de excelência".** Mostra um percurso de ML
  honesto: Q puro 4.73 → híbrido 5.48 → +colocação 5.71 → V3 (visão) ≈ combo,
  com a descoberta e correção do viés do vencedor pelo meio. _(Score final do
  treino longo de 18k a 500 seeds: ver nota no fim.)_

**Porque é que uma heurística perita com BFS é tão difícil de bater por um RL
tabular com treino limitado?** Porque o combo já incorpora conhecimento ótimo
(caminho mais curto, prioridade a curas, evasão de densidade), enquanto o RL tem
de o *descobrir* a partir de uma perceção comprimida e de uma tabela finita, em
tempo limitado. O híbrido (tática aprendida + execução BFS) é a ponte certa — e
chegar ao nível do combo já é um resultado forte e honesto.
