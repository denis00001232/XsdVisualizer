const GAP    = 40;
const STROKE = '#333';
const WIDTH  = 2;

const svg       = document.getElementById('svgLayer');
const rootBlock = document.querySelector('.container-block-root');

/* ---- утилита создания линии ---- */
function createLine(x1,y1,x2,y2){
    const l = document.createElementNS('http://www.w3.org/2000/svg','line');
    l.setAttribute('x1',x1); l.setAttribute('y1',y1);
    l.setAttribute('x2',x2); l.setAttribute('y2',y2);
    l.setAttribute('stroke',STROKE);
    l.setAttribute('stroke-width',WIDTH);
    l.setAttribute('stroke-linecap','round');
    return l;
}

/* ---- авто-размер SVG относительно root ---- */
function fitSvgToRoot(){
    const r = rootBlock.getBoundingClientRect();
    svg.style.width  = `${r.width  + GAP*2}px`;
    svg.style.height = `${r.height + GAP*2}px`;
}
fitSvgToRoot();                          // первая подгонка
//new ResizeObserver(fitSvgToRoot).observe(rootBlock);

/* ---- отрисовка линий с оптимизациями ---- */
let needDraw = false;
function scheduleDraw(){
    if(needDraw) return;
    needDraw = true;
    requestAnimationFrame(()=>{needDraw=false; drawLines();});
}

function drawLines(){
    svg.innerHTML='';
    const conts = Array.from(document.querySelectorAll('.container'));
    const rects = new Map(conts.map(el=>[el,el.getBoundingClientRect()]));
    const sx = window.pageXOffset || document.documentElement.scrollLeft;
    const sy = window.pageYOffset || document.documentElement.scrollTop;
    const frag = document.createDocumentFragment();

    for(const el of conts){
        const block = el.parentElement;
        if(block.classList.contains('container-collapsed')) continue;

        const seq = block.querySelector(':scope > .container-sequence');
        if(!seq || seq.children.length===0) continue;

        const r  = rects.get(el);
        const cx = r.right + sx;
        const cy = r.top   + sy + r.height/2;
        const vx = cx + GAP;

        frag.appendChild(createLine(cx,cy,vx,cy));

        let minY=cy, maxY=cy;

        const kids = Array.from(seq.children, b=>b.querySelector(':scope > .container'));
        for(const kid of kids){
            const kr = rects.get(kid);
            const ky = kr.top + sy + kr.height/2;
            const kx = kr.left + sx;
            frag.appendChild(createLine(kx,ky,kx-GAP,ky));
            minY=Math.min(minY,ky); maxY=Math.max(maxY,ky);
        }
        frag.appendChild(createLine(vx,minY,vx,maxY));
    }
    svg.appendChild(frag);
}

/* ---- сворачивания/разворачивания ---- */
function changeVisibility(btn){
    const block = btn.closest('.container-block');
    if(!block) return;
    const seq = block.querySelector(':scope > .container-sequence');
    if(!seq || seq.children.length===0) return;

    btn.classList.toggle('button-active');
    btn.querySelector('.button-text').textContent =
    btn.classList.contains('button-active')?'+':'-';

    block.classList.toggle('container-collapsed');
    scheduleDraw();
}
function chooseActionClick(event, btn){
    if (event.button === 1) {
        changeVisibility(btn)
    } else if (event.button === 2) {

    }
}

function openInNewWindow(btn) {

}

function hideElement(btn){const b=btn.closest('.button');b&&!b.classList.contains('button-active')&&changeVisibility(b);}
function showElement(btn){const b=btn.closest('.button');b&&b.classList.contains('button-active')&&changeVisibility(b);}
function showAll(){document.querySelectorAll('.button').forEach(showElement); scheduleDraw();}
function hideAll(){document.querySelectorAll('.button').forEach(hideElement); scheduleDraw();}

/* ---- наблюдатели ---- */
window.addEventListener('resize',scheduleDraw,{passive:true});
window.addEventListener('scroll',scheduleDraw,{passive:true});

/* ---- первая отрисовка ---- */
scheduleDraw();
