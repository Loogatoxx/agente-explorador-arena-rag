# Relatório — Machine Learning autónomo (Q-Learning) 100% local

**Data:** 2026-06-25 · **Grupo E** · Projeto Arena 3D RAG

Objetivo: substituir o ajuste por regras/hill-climbing por **aprendizagem por
reforço autónoma**, treinada localmente nos nossos simuladores rápidos, sem
qualquer API na malha de treino.

## 1. Escolha da abordagem — Q-Learning tabular

Avaliámos Q-Learning tabular vs Algoritmo Genético/NEAT. Escolhemos **Q-Learning
tabular** porque:
- **Integra-se na arquitetura**: o agente aprendido é um `AvaliadorAcao`
  (`AvaliadorQ`) que entra na cadeia do `MotorHeuristico` como qualquer regra.
- **100% local e leve**: Q-table em `HashMap`, sem dependências, treina em
  segundos a minutos no MacBook (M4), zero API.
- **Explicável** (oral): estado/ação/recompensa claros + equação de Bellman.
- **Reutiliza os simuladores**: `SimuladorBatalha.treinarEpisodio` corre 1
  episódio de RL com toda a física existente (sem duplicar nada).

## 2. Estados, Ações, Recompensa

- **Ações (4):** MOVER_NORTE / ESTE / SUL / OESTE. (Atacar = mover para cima de um
  rival, já coberto.)
- **Estado discreto (`EstadoQ`)** — ~2160 estados:
  bloqueio adjacente NSEO (4 bits) × direção do alvo mais próximo (5) ×
  direção+ameaça do rival mais próximo (9) × bucket de HP (3).
- **Recompensa (afina a meta):** `r = Δhp + 0.5 (sobreviver)`, com `−50` terminal
  na morte. O `Δhp` capta tudo o que o simulador premeia: cofre (+100), recurso
  (+20), golpe recebido (−15), custo de andar (−1). Maximizar a recompensa
  acumulada ≈ apanhar curas/cofres + evitar dano + sobreviver = boa colocação.

## 3. Como treinar (local, terminal)

```
mvn -o test-compile
java -cp "target/classes:target/test-classes:$GSON" \
     pt.ipt.arena.sim.TreinadorQ 40000 qtable.txt
```

Mostra a curva de aprendizagem, guarda a tabela (`qtable.txt`) e compara, em
greedy, o agente aprendido com o combo entregue, no mesmo campo.

## 4. Resultados (5000 episódios, free-for-all vs 8 perfis + random)

Curva de aprendizagem (colocação média /8, parte de quase-aleatório):

| episódio | epsilon | colocação | 1.ºs % |
|---|---|---|---|
| 1000 | 0.75 | 4.30 | 9.6 |
| 3000 | 0.25 | 4.81 | 12.8 |
| 5000 | 0.05 | 4.83 | 11.6 |

Greedy final (200 seeds, mesmo campo):

| Agente | Colocação /8 | 1.ºs % |
|---|---|---|
| **combo (entregue)** | **5.86** | 33.0 |
| Q-Learning (5k ep.) | 4.73 | 12.5 |

**O sistema aprende** (sobe de ~4.3 para ~4.8), mas o **Q puro ainda não bate o
combo**. É esperado e honesto: o combo navega com **pathfinding BFS** (caminho
ótimo até cofres/curas), enquanto o `EstadoQ` só vê uma **direção grosseira** (4
sentidos) — não contorna paredes tão bem. O plateau ~4.8 sugere o teto desta
codificação de estado.

## 5. Próximos passos para "bater" o combo

1. **Híbrido (recomendado):** manter a navegação BFS do combo e usar o
   Q-Learning **só para a sub-decisão de combate** (lutar/fugir/rematar), onde as
   regras à mão podem ser sub-ótimas. É aí que o ML pode acrescentar valor.
2. **Estado mais rico:** distância ao rival/alvo em buckets, nº de rivais,
   "tenho cura adjacente?" — mais contexto, melhor política (mais estados).
3. **Treino mais longo + tuning da recompensa** (peso de sobreviver vs cofre).

## 6. Conclusão

Entregámos um **sistema de RL autónomo, local e integrado** (não um optimizador de
parâmetros): `EstadoQ` + `TabelaQ` + `AvaliadorQ` + `TreinadorQ`, com testes a
travar a matemática. Mede-se honestamente contra o combo — e o caminho para o
superar (híbrido) está identificado e pronto a implementar.
