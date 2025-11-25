const GAP = 40;
const STROKE = '#333';
const WIDTH = 2;
const RADIUS = 8;
const conts = Array.from(document.querySelectorAll('.container'));
let blockDraw = false;
let svgSizeSet = false;

const svg = document.getElementById('svgLayer');
const rootBlock = document.querySelector('.container-block-root');

function fitSvgToRoot() {
    if (!rootBlock || !svg) return;

    if (svgSizeSet) {
        drawLines();
        return;
    }

    const collapsedBlocks = [];
    document.querySelectorAll('.container-collapsed').forEach(block => {
        collapsedBlocks.push(block);
        block.classList.remove('container-collapsed');
    });

    requestAnimationFrame(() => {
        const body = document.body;
        const html = document.documentElement;

        const width = Math.max(
            body.scrollWidth,
            body.offsetWidth,
            html.clientWidth,
            html.scrollWidth,
            html.offsetWidth,
            rootBlock.scrollWidth
        );

        const height = Math.max(
            body.scrollHeight,
            body.offsetHeight,
            html.clientHeight,
            html.scrollHeight,
            html.offsetHeight,
            rootBlock.scrollHeight
        );

        const gap2 = GAP * 2;

        svg.style.width = `${width + gap2}px`;
        svg.style.height = `${height + gap2}px`;
        svg.style.position = 'absolute';
        svg.style.top = '0';
        svg.style.left = '0';
        svg.style.pointerEvents = 'none';

        svg.setAttribute('width', width + gap2);
        svg.setAttribute('height', height + gap2);
        svg.setAttribute('viewBox', `0 0 ${width + gap2} ${height + gap2}`);
        svg.setAttribute('preserveAspectRatio', 'xMinYMin meet');

        svgSizeSet = true;

        collapsedBlocks.forEach(block => {
            block.classList.add('container-collapsed');
        });

        drawLines();
    });
}

function setupContainerInfo() {

}

function copyPath(container) {
    const path = [];
    while (true) {

    }
}

function createPathElement(pathData) {
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', pathData);
    path.setAttribute('stroke', STROKE);
    path.setAttribute('stroke-width', WIDTH);
    path.setAttribute('stroke-linejoin', 'round');
    path.setAttribute('stroke-linecap', 'round');
    path.setAttribute('fill', 'none');
    path.setAttribute('vector-effect', 'non-scaling-stroke');
    return path;
}

function drawLines() {
    if (blockDraw) {
        return;
    }
    if (!svg || !conts.length) return;

    svg.innerHTML = '';

    const rects = new Map();
    for (const el of conts) {
        rects.set(el, el.getBoundingClientRect());
    }

    const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
    const scrollY = window.pageYOffset || document.documentElement.scrollTop;

    for (const el of conts) {
        const block = el.parentElement;
        if (!block || block.classList.contains('container-collapsed')) continue;

        const seq = block.querySelector(':scope > .container-sequence');
        if (!seq || !seq.children.length) continue;

        const r = rects.get(el);
        if (!r) continue;
        if (r.width === 0 && r.height === 0) continue; // родитель скрыт/невидим → не рисуем

        const cy = r.top + scrollY + r.height / 2;
        const cx = r.right + scrollX;
        const vx = cx + GAP;

        const kidsData = [];
        let minY = cy;
        let maxY = cy;

        for (const childBlock of seq.children) {
            const kid = childBlock.querySelector(':scope > .container');
            if (!kid) continue;

            const kr = rects.get(kid);
            if (!kr) continue;
            if (kr.width === 0 && kr.height === 0) continue; // ребёнок скрыт/невидим

            const ky = kr.top + scrollY + kr.height / 2;
            const kx = kr.left + scrollX;

            kidsData.push({ kx, ky });

            if (ky < minY) minY = ky;
            if (ky > maxY) maxY = ky;
        }

        // Если нет ни одного видимого/валидного ребёнка — не рисуем вообще ничего
        if (!kidsData.length) {
            continue;
        }

        let pathData = '';

        // Горизонтальная линия от родителя к вертикальному стволу
        pathData += `M${cx},${cy}L${vx},${cy}`;

        // Один ребёнок — просто прямая линия без ствола
        if (kidsData.length === 1) {
            const { kx, ky } = kidsData[0];
            pathData += `M${vx},${ky}L${kx},${ky}`;
            svg.appendChild(createPathElement(pathData));
            continue;
        }

        // Несколько детей — вертикальный ствол с учётом закругления краёв
        if (minY < cy || maxY > cy) {
            pathData += `M${vx},${minY + RADIUS}L${vx},${maxY - RADIUS}`;
        }

        // Горизонтальные ветви к детям
        for (let i = 0; i < kidsData.length; i++) {
            const { kx, ky } = kidsData[i];

            const isTop = (ky === minY && ky < cy);
            const isBottom = (ky === maxY && ky > cy);

            if (isTop) {
                pathData += `M${vx},${ky + RADIUS}Q${vx},${ky} ${vx + RADIUS},${ky}L${kx},${ky}`;
            } else if (isBottom) {
                pathData += `M${vx},${ky - RADIUS}Q${vx},${ky} ${vx + RADIUS},${ky}L${kx},${ky}`;
            } else {
                pathData += `M${vx},${ky}L${kx},${ky}`;
            }
        }

        svg.appendChild(createPathElement(pathData));
    }
}

function changeVisibility(btn) {
    const block = btn.closest('.container-block');
    if (!block) return;

    const seq = block.querySelector(':scope > .container-sequence');
    if (!seq || !seq.children.length) return;

    const isActive = btn.classList.toggle('button-active');
    const textEl = btn.querySelector('.button-text');
    if (textEl) {
        textEl.textContent = isActive ? '+' : '-';
    }

    block.classList.toggle('container-collapsed');
    drawLines();
}

function hideElement(btn) {
    const b = btn.closest('.button');
    if (b && !b.classList.contains('button-active')) {
        changeVisibility(b);
    }
}

function showElement(btn) {
    const b = btn.closest('.button');
    if (b && b.classList.contains('button-active')) {
        changeVisibility(b);
    }
}

function showAll() {
    blockDraw = true;
    document.querySelectorAll('.button').forEach(showElement);
    blockDraw = false;
    drawLines();
}

function hideAll() {
    blockDraw = true;
    document.querySelectorAll('.button').forEach(hideElement);
    blockDraw = false;
    drawLines();
}

let resizeTimeout;
window.addEventListener(
    'resize',
    () => {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            drawLines();
        }, 100);
    },
    { passive: true }
);

if (window.visualViewport) {
    let zoomTimeout;
    window.visualViewport.addEventListener('resize', () => {
        clearTimeout(zoomTimeout);
        zoomTimeout = setTimeout(() => {
            drawLines();
        }, 100);
    });
}

fitSvgToRoot();