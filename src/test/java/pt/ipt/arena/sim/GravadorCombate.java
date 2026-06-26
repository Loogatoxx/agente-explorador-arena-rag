package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Gera um REPLAY visual de combate (HTML auto-suficiente) com uma DERROTA e uma
 * VITÓRIA do combo, para analisar a olho porque perdemos. Bot azul = NOSSO.
 *
 * Uso: java ... GravadorCombate  → escreve docs/simulacao_combate.html
 */
public final class GravadorCombate {

    private GravadorCombate() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, Supplier<MotorHeuristico>> perfis = CombatePerfis.perfis();
        List<String> nomes = new ArrayList<>(perfis.keySet());
        List<Supplier<MotorHeuristico>> advs = new ArrayList<>(perfis.values());
        Supplier<MotorHeuristico> combo = () -> new MotorHeuristico("combo");

        // Procura uma DERROTA clara (5.º lugar ou pior, por morte) — mais
        // instrutiva — e uma VITÓRIA. Vai relaxando o critério se não achar.
        String derrota = procurar(combo, nomes, advs, 4); // colocacao >= 4 (5.º+)
        if (derrota == null) {
            derrota = procurar(combo, nomes, advs, 2); // 3.º+
        }
        if (derrota == null) {
            derrota = procurar(combo, nomes, advs, 1); // qualquer não-vitória
        }
        String vitoria = null;
        for (int s = 0; s < 400 && vitoria == null; s++) {
            SimuladorBatalha.Gravacao g = SimuladorBatalha.gravarPartidaJson(combo, nomes, advs, s, 17, 8, 4);
            if (g.colocacao == 0) {
                vitoria = g.json;
            }
        }

        geraHtml(derrota, vitoria);
    }

    /** Devolve o JSON da 1.ª partida em que colocamos >= colocacaoMin e morremos. */
    private static String procurar(Supplier<MotorHeuristico> combo, List<String> nomes,
                                   List<Supplier<MotorHeuristico>> advs, int colocacaoMin) {
        for (int s = 0; s < 400; s++) {
            SimuladorBatalha.Gravacao g = SimuladorBatalha.gravarPartidaJson(combo, nomes, advs, s, 17, 8, 4);
            if (g.colocacao >= colocacaoMin && g.morteNosso >= 0) {
                return g.json;
            }
        }
        return null;
    }

    private static void geraHtml(String derrota, String vitoria) throws Exception {
        String html = PAGINA
                .replace("\"__DERROTA__\"", derrota == null ? "null" : derrota)
                .replace("\"__VITORIA__\"", vitoria == null ? "null" : vitoria);
        Path saida = Path.of("docs/simulacao_combate.html");
        Files.writeString(saida, html);
        System.out.println("Escrito " + saida.toAbsolutePath());
    }

    private static final String PAGINA = """
<!DOCTYPE html><html lang="pt"><head><meta charset="utf-8">
<title>Replay de combate — Grupo E</title>
<style>
 body{font-family:-apple-system,Arial,sans-serif;background:#0f1115;color:#e6e6e6;margin:0;padding:16px}
 h1{font-size:18px;margin:0 0 4px;color:#7fb4ff}
 .sub{color:#9aa4b2;font-size:12px;margin-bottom:10px}
 #wrap{display:flex;gap:18px;flex-wrap:wrap}
 canvas{background:#0a0c10;border:1px solid #222;border-radius:8px}
 .panel{min-width:230px}
 button,select{background:#1b2330;color:#e6e6e6;border:1px solid #2c3850;border-radius:6px;padding:6px 10px;font-size:13px;cursor:pointer}
 button:hover{background:#243049}
 .row{margin:8px 0;display:flex;gap:8px;align-items:center;flex-wrap:wrap}
 .status{background:#141a24;border-left:3px solid #7fb4ff;padding:8px 10px;border-radius:6px;font-size:13px;line-height:1.5}
 .leg span{display:inline-block;width:11px;height:11px;border-radius:50%;margin-right:5px;vertical-align:middle}
 input[type=range]{width:240px}
</style></head><body>
<h1>Replay de combate — analisar porque perdemos</h1>
<div class="sub">Bot AZUL = NOSSO (combo "tartaruga"). Cada movimento custa 1 HP; ficar parado custa 0.</div>
<div id="wrap">
 <canvas id="cv" width="560" height="560"></canvas>
 <div class="panel">
  <div class="row">
   <select id="modo"><option value="derrota">❌ Derrota (analisar)</option><option value="vitoria">✅ Vitória</option></select>
  </div>
  <div class="row">
   <button id="play">▶︎ Play</button><button id="prev">⟨ -1</button><button id="next">+1 ⟩</button>
   <label>vel <select id="vel"><option>1</option><option selected>2</option><option>4</option><option>8</option></select>x</label>
  </div>
  <div class="row"><input type="range" id="slider" min="0" value="0"><span id="turno">t=0</span></div>
  <div class="status" id="status"></div>
  <div class="status leg" style="border-left-color:#3a3">
   <b>Legenda</b><br>
   <span style="background:#3b82f6"></span>NOSSO &nbsp; <span style="background:#ef4444"></span>rival &nbsp;
   <span style="background:#555"></span>morto<br>
   <span style="background:#eab308"></span>cofre (+100) &nbsp; <span style="background:#22c55e"></span>recurso (+20) &nbsp;
   <span style="background:#3a3f4b;border-radius:2px"></span>parede
  </div>
 </div>
</div>
<script>
const REPLAYS = { derrota: "__DERROTA__", vitoria: "__VITORIA__" };
const cv = document.getElementById('cv'), ctx = cv.getContext('2d');
let modo='derrota', dados=REPLAYS[modo], f=0, tocando=false, timer=null;
const cores=['#3b82f6','#ef4444','#f97316','#a855f7','#14b8a6','#eab308','#ec4899','#84cc16','#06b6d4'];

function setModo(m){ modo=m; dados=REPLAYS[m]; f=0; document.getElementById('slider').max=(dados? dados.frames.length-1:0); render(); }
function cell(){ return cv.width / (dados.n); }
function render(){
 ctx.clearRect(0,0,cv.width,cv.height);
 if(!dados){ ctx.fillStyle='#aaa'; ctx.fillText('sem dados',20,20); return; }
 const cs=cell();
 ctx.fillStyle='#3a3f4b';
 for(const [x,y] of dados.paredes) ctx.fillRect(x*cs,y*cs,cs-1,cs-1);
 const fr=dados.frames[f];
 ctx.fillStyle='#eab308'; for(const [x,y] of fr.c){ disco(x,y,cs,'#eab308'); }
 ctx.fillStyle='#22c55e'; for(const [x,y] of fr.r){ disco(x,y,cs,'#22c55e'); }
 for(let i=0;i<fr.b.length;i++){
   const [x,y,hp,vivo]=fr.b[i];
   if(!vivo && hp<=0){ disco(x,y,cs,'#555'); continue; }
   disco(x,y,cs, i===0? '#3b82f6' : cores[(i)%cores.length]);
   // barra de HP
   ctx.fillStyle='#111'; ctx.fillRect(x*cs+2,y*cs-1,cs-4,3);
   ctx.fillStyle= i===0? '#7fb4ff':'#f87171'; ctx.fillRect(x*cs+2,y*cs-1,(cs-4)*Math.min(1,hp/250),3);
   if(i===0){ ctx.strokeStyle='#fff'; ctx.lineWidth=2; ctx.beginPath(); ctx.arc(x*cs+cs/2,y*cs+cs/2,cs*0.42,0,7); ctx.stroke(); }
 }
 document.getElementById('turno').textContent='t='+f+' / '+(dados.frames.length-1);
 const nos=fr.b[0];
 const vivosR=fr.b.slice(1).filter(b=>b[3]===1).length;
 document.getElementById('status').innerHTML =
   '<b>Seed '+dados.seed+'</b> — '+(dados.coloc===0?'✅ ganhámos':'❌ '+(dados.coloc+1)+'.º lugar')
   + (dados.morte>=0? ' (morremos no turno '+dados.morte+')':' (sobrevivemos)')
   +'<br>NOSSO: pos ('+nos[0]+','+nos[1]+') · HP '+nos[2]+(nos[3]? '':' ☠️')
   +'<br>rivais vivos agora: '+vivosR;
}
function disco(x,y,cs,c){ ctx.fillStyle=c; ctx.beginPath(); ctx.arc(x*cs+cs/2,y*cs+cs/2,cs*0.38,0,7); ctx.fill(); }
function passo(d){ if(!dados)return; f=Math.max(0,Math.min(dados.frames.length-1,f+d)); document.getElementById('slider').value=f; render(); }
function play(){ tocando=!tocando; document.getElementById('play').textContent=tocando?'⏸ Pause':'▶︎ Play';
 if(timer)clearInterval(timer);
 if(tocando){ const v=+document.getElementById('vel').value; timer=setInterval(()=>{ if(f>=dados.frames.length-1){play();return;} passo(1); }, 220/v); } }
document.getElementById('modo').onchange=e=>setModo(e.target.value);
document.getElementById('play').onclick=play;
document.getElementById('prev').onclick=()=>passo(-1);
document.getElementById('next').onclick=()=>passo(1);
document.getElementById('slider').oninput=e=>{ f=+e.target.value; render(); };
setModo('derrota');
</script></body></html>
""";
}
