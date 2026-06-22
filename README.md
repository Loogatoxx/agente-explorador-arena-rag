# Agente Explorador na Arena 3D RAG

Projeto Final da unidade curricular de **Programação III** (IPT).

Cliente autónomo em **Java** que atua como o "cérebro" cognitivo de um robô
explorador na **Arena 3D RAG**. O agente liga-se por **REST API** ao servidor
central da Arena, percebe o mundo (radar/telemetria), decide de forma
heurística o próximo movimento e resolve enigmas dos Terminais de Plasma
através de um **pipeline RAG** executado num motor de IA local (**Ollama**).

## Arquitetura (Topologia Híbrida)
- **Servidor Central (Cloud / OVH)** — árbitro do "mundo físico" (não é nosso).
- **Cliente Autónomo (Java, local)** — este repositório; o sistema nervoso do robô.
- **Motor de IA (Ollama, local, porta 11434)** — embeddings e geração (LLM).

## Estrutura de classes (POO / SRP)
| Classe | Responsabilidade |
|--------|------------------|
| `AgenteExplorador` | Orquestrador do ciclo Sense-Think-Act (Main). |
| `ArenaClient` | Comunicação HTTP REST com a Arena (registo, perceção, ação, unlock). |
| `OllamaClient` | Comunicação com a IA local (embeddings + geração RAG). |
| `DocumentoVetorial` | Modelo de dados: chunk de texto + vetor de embeddings. |
| `PainelMapaCalor` | Interface visual (Swing/Graphics2D): telemetria + mapa de calor. |

## Tecnologias
- Java 17+ (`java.net.http.HttpClient`)
- Maven
- Gson 2.10.1 (JSON)
- Ollama: `nomic-embed-text` (embeddings) + `qwen2.5-coder:0.5b-instruct` (LLM)

## Pré-requisitos
1. JDK 17 ou superior.
2. Ollama instalado e a correr (`http://localhost:11434`).
3. Modelos descarregados:
   ```
   ollama pull qwen2.5-coder:0.5b-instruct-q4_K_M
   ollama pull nomic-embed-text
   ```

## Interface (PainelMapaCalor)
Dashboard de telemetria local em Java Swing: radar centrado no agente, mapa de
calor das células visitadas, paredes, recursos, cofres e rivais, mais o estado
(posição, HP, contagens).

![Dashboard de telemetria](docs/dashboard.png)

*(render ilustrativo com um estado de jogo de exemplo.)*

## Documentação do projeto
- `DIARIO_DE_BORDO.md` — histórico cronológico completo do desenvolvimento.
- `auditoria_ia/` — auditoria individual do uso de IA (prompts e respostas).
