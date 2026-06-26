# Relatório DETALHADO — Porque é que ainda perdemos? (e como subir)

**Data:** 2026-06-26 · **Grupo E** · Projeto Arena 3D RAG

Pergunta do Carlos: se só ~3,5% das derrotas são azar de spawn, porque é que não
ganhamos mais? De que MORRE o bot, se não for o spawn?

## 1. A distribuição real (`AnaliseProfunda`, 400 seeds, 9 bots)

Com a estratégia tartaruga atual:

| Colocação | % |
|---|---|
| **1.º (vitória)** | **66,3%** |
| 2.º | 6,8% |
| 3.º | 5,5% |
| 4.º-7.º | ~3-4% cada |
| 8.º | 4,3% |
| 9.º (último) | 3,0% |

**Nota importante:** num free-for-all de 9 bots, 66% de 1.ºs lugares é
**dominante** (o acaso daria ~11%). As "não-vitórias" são sobretudo 2.º/3.º
lugares (quase-vitórias), e só ~3% são último lugar. Não há um "teto de 96,5%" de
*win rate*: em battle royale, 2.º não é uma derrota como num duelo.

## 2. De que morremos (a resposta à pergunta)

Das não-vitórias (135 em 400):
- **94,8% = morremos** (só 5,2% foi "sobrevivemos mas com menos HP que outro").
- Dessas mortes, **só 22% são precoces** (<turno 60). **78% são mortes TARDIAS.**
- **Quem ganha quando não ganhamos:** `Equilibrado` (28,9%) e `Oportunista`
  (20,7%) — bots NEUTRO que atacam quando lhes dá jeito.

**Conclusão:** não perdemos por azar de spawn nem por desempate de HP. Morremos
no **meio-fim da mêlée**, apanhados por um bot que se compromete connosco.

## 3. Soluções testadas (com dados)

### 3a. Margem de conservação (tartaruga) — O GRANDE GANHO ✅
Conservar HP mesmo um pouco atrás: battle royale **6.20 → ~6.85/8**, 1v1
**68% → 84,7%**. Já aplicado (margem -150).

### 3b. Margem de combate — testámos "ripostar mais": PIOR ❌
Hipótese: como conservamos HP, devíamos converter a vantagem atacando quando
engajados. Varrimento (400 seeds, com tartaruga):

| Margem de combate | Colocação /8 |
|---|---|
| **90 (atual, quase só foge)** | **6.83** |
| 45 | 6.70 |
| 30 | 6.66 |
| 0 | 6.21 |
| -15 | 5.99 |
| -30 | 5.59 |

**Atacar mais MATA mais.** Fugir (margem 90) é o menos mau quando alguém nos
engaja — atacar troca golpes e, em mêlée, vira-nos presa. Mantém-se 90.

### 3c. Onde estamos
O par (conservação -150, combate 90) está **perto do teto** desta família de
estratégias. As mortes tardias são, em boa parte, a **variância intrínseca** de
um melée de 9 bots: às vezes dois rivais convergem sobre nós e fogir não chega.

## 4. Ideias para subir mais (futuras / a testar)

1. **Posição de camp defensiva:** campar em parede RASA (3 saídas) em vez de um
   CANTO (2 saídas, fácil de encurralar) — mais hipóteses de fuga quando um rival
   chega. Micro-otimização plausível.
2. **Relocalização proativa:** ao detetar um rival a aproximar-se do camp, mudar
   de zona ANTES de ser fixado (em vez de esperar que entre no alcance).
3. **Memória de posições dos rivais** para campar LONGE da última densidade vista.

> O resultado 3b avisa que estas ideias têm **upside limitado** (mexer no combate
> tende a piorar); qualquer uma só entra se MEDIR melhor que 6.83.

## 5. Análise VISUAL — `docs/simulacao_combate.html`

Replay animado (auto-suficiente, abre no browser) com uma **derrota** e uma
**vitória** do combo. Bot AZUL = NOSSO. Permite ver, turno a turno, como morremos
(quem nos apanha, em que zona, se ficámos encurralados). Gerado por
`pt.ipt.arena.sim.GravadorCombate`.

## 6. RNG a nosso favor — conclusão honesta
Não controlamos o spawn no servidor real. A jogada certa é **baixar a variância**
(a tartaruga fá-lo: subiu as vitórias de ~40% para ~66%). O resíduo (mêlée
azarada, spawn mau) é largamente irredutível por estratégia — e 66% de 1.ºs num
campo de 9 já é um resultado de topo.

## 7. Reproduzir
```
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.AnaliseProfunda 400
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.GravadorCombate   # gera o replay
```
