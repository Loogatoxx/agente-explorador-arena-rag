# Relatório — Análise de combate, variantes e RNG

**Data:** 2026-06-26 · **Grupo E** · Projeto Arena 3D RAG

Partimos de uma observação real (ao combater o professor, o robô gastava HP a
andar à toa) e fizemos uma análise **medida** para subir o desempenho do combo.

## 1. Porque é que perdíamos? (`AnaliseBatalha`, 200 seeds)

Classificando as derrotas do combo (antes da tartaruga):
- **86,8% = "morte tarde"** (morremos depois do turno 60). Sobrevivíamos ao caos
  inicial, mas **sangrávamos HP a mover-nos** e acabávamos por cair.
- **11,6% = morte cedo** (mêlée nos primeiros turnos).
- **1,7% = vivos mas com HP baixo** (perda no desempate).

Os PIORES jogos (último lugar) eram **maus spawns**: morríamos nos turnos 22-67
com os **8 rivais ainda vivos** (somos o primeiro a morrer) — nascemos encurralados.

## 2. A correção que venceu: conservação "TARTARUGA"

Cada movimento custa 1 HP; ficar parado contra uma parede (movimento bloqueado) custa 0.
Varremos a **margem de conservação** (quanto à frente é preciso estar para campar),
em battle royale, 400 seeds paralelos:

| Margem | Colocação /8 | 1.ºs % |
|---|---|---|
| 1000 (sem conservação) | 6.17 | 38 |
| 0 (conservar só à frente) | 6.21 | 38 |
| -30 | 6.40 | 53 |
| -60 | 6.56 | 58 |
| -100 | 6.80 | 67 |
| **-150 (ESCOLHIDO)** | **6.82** | **70** |
| -1000 (campar sempre) | 6.91 | 70 |

**Conservar mesmo um pouco atrás é muito melhor.** Razão: andar à toa só perde a
vida; ao ficar parado, os bots agressivos **entre-matam-se** e nós sobrevivemos no
topo. Escolhemos **-150** (≈ ótimo, e ainda volta a procurar loot/curas se estiver
>150 HP atrás do líder — mais robusto que campar sempre).

### Resultado holístico (combo com tartaruga)
- Navegação SOLO: **30/30** (a tartaruga nunca dispara em solo).
- Battle royale: **6.20 → ~6.85/8** (≈69% de 1.ºs lugares).
- 1v1 médio: **68% → 84,7%** — passámos a ganhar **até aos agressores** (60-70%),
  porque conservamos HP entre engajamentos enquanto o adversário sangra a andar.

## 3. Outras mecânicas consideradas

- **Negação de loot:** apanhar cofres mesmo a 250 HP (cap) para os negar ao
  inimigo — já o fazemos (o `AvaliadorAlvo` persegue cofres conhecidos antes de
  campar).
- **Remate oportunista:** se um rival fraco (≤15 HP) entra no radar enquanto
  campamos, o `AvaliadorBatalha` (antes da conservação) abate-o de graça.
- **Campar no CANTO** (2 paredes = menos ângulos de ataque) em vez de parede
  rasa: micro-otimização possível no futuro.

## 4. RNG — pô-la a nosso favor

As derrotas que sobram (~3,5%) são **maus spawns** (nascemos encurralados por
agressores e morremos antes de nos pormos a salvo). No **servidor real não
escolhemos o spawn**, por isso a melhor jogada é **reduzir a variância**:
- A **tartaruga** já elimina a maior fonte de perda (o sangramento por movimento),
  subindo as vitórias de 39,5% para ~64%.
- Os maus spawns são quase **irredutíveis** por estratégia (estás cercado à
  partida). Hipótese não recomendada: re-registar para "re-rolar" o spawn — frágil
  e contra o espírito do exercício.
- Conclusão honesta: maximizamos as hipóteses jogando de forma de **baixa
  variância** (conservar, não procurar luta), e aceitamos um resíduo de azar de spawn.

## 5. Reproduzir

```
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.AnaliseBatalha 200
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.VariantesConservacao 400
```
