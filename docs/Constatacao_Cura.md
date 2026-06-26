# Constatação MEDIDA — mecânica de cura (servidor real)

**Data:** 2026-06-26 · sala FD0305 · robô `DiagnosticoCura`

Método: o robô queimou HP a oscilar numa zona sem loot (cada movimento = −1 HP)
até ~25, e depois abriu um cofre, registando o HP antes/depois.

## Resultado (definitivo)

| Item | Antes | Depois | Delta | Conclusão |
|---|---|---|---|---|
| **Cofre** | 37 | 137 | **+100** | **+100 FIXO** (não enche ao máximo). Teto 250. |
| Recurso | 24 | ~44 | ~+20 | +20 fixo (inferido; bate com o modelo). |

A 37 HP o cofre deu 137 (37+100), **não 250** → é um bónus fixo de +100, com teto
em 250. Isto **valida o nosso simulador** (`b.hp = min(250, b.hp + 100)`), logo os
benchmarks estão certos.

## Meta que isto destrava (cofre-timing)

- **O valor REAL de um cofre para nós = min(100, 250 − HP_atual).** A 250 HP vale
  **0** (desperdiçado pelo teto); a ≤150 HP vale **+100** (cheio).
- **Abrir cofres com HP ≤ 150** para aproveitar os +100 inteiros. Com HP alto, o
  cofre vale sobretudo como **NEGAÇÃO** (tirar os +100 ao inimigo), não como cura.
- **Tensão com a tartaruga:** ao conservar HP perto do máximo, as nossas próprias
  curas de cofre ficam quase desperdiçadas → o cofre passa a ser **arma de
  negação** (e a nossa velocidade de Ollama no Mac dá-nos vantagem para os negar
  primeiro), e cura útil só quando estamos em baixo.

## Reproduzir
```
java -cp "target/classes:target/test-classes:$GSON" pt.ipt.arena.DiagnosticoCura <SALA> diagCura
```
