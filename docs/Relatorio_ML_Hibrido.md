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

## 5. Como treinar (local)

```
mvn -o test-compile
java -cp "target/classes:target/test-classes:$GSON" \
     pt.ipt.arena.sim.TreinadorQ 20000 hibrido qhib.txt
```

## 6. Conclusão

O híbrido é a arquitetura certa: **navegação BFS do heurístico + combate
aprendido por RL**. Mede-se honestamente contra o combo — e está agora
ombro-a-ombro com a melhor configuração afinada à mão.
