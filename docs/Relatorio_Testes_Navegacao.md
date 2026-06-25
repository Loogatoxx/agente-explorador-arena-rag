# Relatório de Testes — Navegação e Exploração (deslocamento por saltos)

**Data:** 2026-06-25 · **Grupo E** · Projeto Arena 3D RAG

Objetivo desta ronda: o robô varria o mapa **linha a linha** (lento). Sabendo que
o radar vê várias casas à volta, queremos que ele **salte** em vez de pisar cada
casa — sem nunca deixar um cofre por descobrir.

---

## 1. Alcance real do radar (medido ao vivo)

Medido no servidor real (sala 9B8860), com o robô em (17,3):

- Objetos vistos: `dx, dy ∈ [-3, +3]` → **quadrado 7×7** centrado no robô
  (distância de Chebyshev **3**; ~49 casas).
- O simulador modela o radar como **4.5 euclidiano com oclusão** — coerente: o
  quadrado de raio 3 cabe inteiro dentro de 4.5 (canto (3,3)=4.24 < 4.5).

**Conclusão:** marcar como "vistas" as casas até **raio 3** é seguro — são todas
realmente visíveis. Marcar raio 4 já inclui cantos (4,4)=5.66 > 4.5 que **não**
são vistos → sobre-declaração → risco de saltar um cofre escondido.

## 2. A mudança

- Antes: a exploração ia para a casa transitável **não pisada** mais próxima →
  obrigava a pisar todas as casas (varrimento linha a linha).
- Agora: cada paragem marca como **VISTAS** as casas no quadrado de raio R; a
  exploração vai para a casa **não vista** mais próxima → o agente salta ~R+1
  casas entre varrimentos, cobrindo tudo sem buracos.
- `R` = `MotorHeuristico.DEFAULT_RAIO_VISAO` (escolhido por benchmark abaixo).

## 3. Resultados do benchmark (30 cartas 24×24, 3 cofres)

Métrica: cofres resolvidos e **turnos totais** (menor = melhor).

| Config | Resolvidos | Score (turnos) | Notas |
|---|---|---|---|
| **Guloso** (clássico) | **6/30** | 73 116 | a armadilha — ver §5 |
| raio 0 (pisar cada casa) | 29/30 | 9 467 | a versão "otimizada" anterior |
| raio 1 | 30/30 | 3 570 | |
| raio 2 | 30/30 | 2 867 | |
| **raio 3** | **30/30** | **2 445** | **escolhido** |
| raio 4 | 30/30 | 2 460 | sem ganho, sobre-declara |
| raio 5 | 30/30 | 2 588 | salta demais, anda para trás |

Validação em **120 cartas** (anti-viés do vencedor):

| Config | Resolvidos | Score |
|---|---|---|
| raio 0 | 113/120 | 43 222 |
| raio 2 | 120/120 | 10 787 |
| **raio 3** | **120/120** | **9 647** |
| raio 4 | 120/120 | 9 337 |

Raio 3 e 4 empatam em cobertura (120/120); raio 4 é ~3% mais rápido em simulação
**mas** sobre-declara casas que o radar real não vê. Escolhemos **raio 3**:
princípio físico sólido (= alcance real), 120/120, sem risco de buraco.

## 4. Ganho final (A/B oficial, guloso vs otimizado raio-3)

- Resolvidos: **6/30 → 30/30**.
- Turnos médios (cenários que ambos resolvem): **186 → 53** (−72%).
- Score global: **73 116 → 2 445** → **−96,7%**.
- Vs versão anterior (raio 0, 29/30, 9 467): **+1 cenário e ~3,9× mais rápido**.

## 5. Lições (para NÃO repetir erros)

1. **O guloso puro faz 6/30.** Ir sempre ao alvo visível mais próximo deixa o
   robô preso em bolsas locais e nunca cobre o mapa. Exploração sistemática
   (fronteiras por BFS) é obrigatória.
2. **Otimizar a cobertura é uma faca de dois gumes.** Saltar demais (raio ≥ 5,
   ou sobre-declarar como raio 4 no servidor real) volta a **falhar cofres** —
   o mesmo tipo de erro do guloso, por outra via. Por isso medimos vários raios
   e exigimos **30/30 / 120/120** antes de aceitar ganho de velocidade.
3. **Medir em poucos seeds engana (viés do vencedor).** Em 30 cartas o raio 3
   ganhava; em 120, raio 4 passava à frente por pouco. Validar sempre em amostra
   grande antes de fixar um parâmetro.
4. **Ancorar parâmetros na realidade.** O raio 3 não é só o "melhor número": é o
   alcance **medido** do radar — o que o torna robusto fora da simulação.

## 6. Como reproduzir

```
# Suite de testes (inclui travas de regressão de navegação):
mvn -o test
# Tabela A/B guloso vs otimizado:
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.sim.BenchmarkNavegacao
```

Travas automáticas: `BenchmarkNavegacaoTest.exploracaoComRadarSuperaPisarCadaCasa`
exige 30/30 e >2× mais rápido que o raio 0.
