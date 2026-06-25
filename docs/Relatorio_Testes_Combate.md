# Relatório de Testes — Combate (1v1 e Battle Royale)

**Data:** 2026-06-25 · **Grupo E** · Projeto Arena 3D RAG

Objetivo: confirmar/melhorar o desempenho de combate do agente em **1v1** e em
**battle royale**, contra vários perfis de adversário **incluindo bots aleatórios
(random)**. Driver: `CombatePerfis` (usa `SimuladorBatalha.avaliarContra`, que
mede qualquer motor — incluindo o agente ENTREGUE, o "combo").

## 1. Montagem

- **Campo de adversários (8):** Agressor, Cacador, Equilibrado, Oportunista,
  Conservador, Cauteloso, **Random1, Random2**.
- **Candidatos nossos:** `ENTREGUE(combo)` (a configuração entregue:
  Reflexos→Batalha(0,90,remate)→Aspirador→Esquiva→Alvo→Canto→Exploração),
  `Evasivo` (EVITAR, margem 30), `CacadorSel` (CACAR, 20/30), `Cauteloso` (EVITAR 60/30).
- **Métricas:** battle royale → colocação média (0..8) e % de 1.ºs lugares;
  1v1 → taxa de vitória (%) contra cada perfil. 150 seeds (BR) / 250 seeds (1v1).

## 2. Resultados

### Battle royale (9 bots, 150 seeds)

| Candidato | Colocação (/8) | 1.ºs % | Últimos % |
|---|---|---|---|
| **ENTREGUE(combo)** | **6.247** | **39.3** | 2.7 |
| Cauteloso | 5.460 | 20.0 | 2.7 |
| Evasivo | 5.413 | 16.7 | 2.7 |
| CacadorSel | 4.267 | 5.3 | 4.0 |

### 1v1 — taxa de vitória (%) por perfil, 250 seeds

| Candidato | Ag. | Cac. | Equi. | Opor. | Cons. | Caut. | Rnd1 | Rnd2 | **Média** |
|---|---|---|---|---|---|---|---|---|---|
| **ENTREGUE(combo)** | 38 | 43 | 38 | 59 | 75 | 75 | 90 | 90 | **63.5** |
| Evasivo | 32 | 35 | 28 | 44 | 61 | 61 | 86 | 86 | 54.1 |
| Cauteloso | 30 | 35 | 28 | 44 | 61 | 61 | 85 | 86 | 53.7 |
| CacadorSel | 26 | 29 | 22 | 50 | 58 | 59 | 76 | 76 | 49.6 |

**O agente ENTREGUE é o melhor nas DUAS frentes** — battle royale (colocação e
1.ºs lugares) e 1v1 (média de vitórias). Domina o random (90%) e os perfis
prudentes (75%). A única fraqueza relativa é o 1v1 contra **agressores puros**
(Agressor/Equilibrado ~38%) — mas mesmo aí continua a ser o melhor candidato no
conjunto, e em battle royale (o modo real da competição) está claramente à frente.

## 3. Tentativa de melhoria (e porque foi revertida)

Hipótese: a `Esquiva` foge mesmo de um rival ÚNICO; num duelo isso parecia mau.
Testámos gatá-la para só fugir com **2+ rivais**.

| | BR colocação | 1v1 média | 1v1 vs Agressor | 1v1 vs Random |
|---|---|---|---|---|
| Combo original | **6.247** | **63.5** | 38 | 90 |
| Esquiva gated (≥2) | 5.993 | 55.7 | **18** | 79 |

**Pior em tudo.** Fugir de um rival único é, na verdade, **kiting** — evita
golpes e dá tempo para apanhar curas. Revertido. **Lição:** não "otimizar" por
intuição um comportamento já medido como bom (mesmo erro de fundo do guloso 6/30
e do salto-a-mais na navegação).

## 4. Conclusão

- O **combo entregue já é o ótimo** entre as configurações testadas, em 1v1 e em
  battle royale, contra perfis variados **e random**. Nenhum arquétipo do espaço
  de parâmetros (limiar/margem/postura/remate) o supera — daí mantermos a entrega.
- Perseguir o 1v1 contra agressores puros (mudando a postura para mais agressiva)
  degradaria a **dominância em battle royale**, que é o modo da competição real.
  Decisão deliberada: **não sacrificar o primário pelo secundário**.

## 5. Reproduzir

```
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.CombatePerfis 150 250
```

Trava automática: `CombateTest.comboLideraBattleRoyaleE1v1` garante que o combo
continua a bater os arquétipos em battle royale e na média de 1v1.
