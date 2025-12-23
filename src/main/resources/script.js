const GAP = 20;
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
    if (blockDraw) { //Блокируем рисовку если есть блок
        return;
    }
    if (!svg || !conts.length) return; //не рисуем если нет svg или нет контейнеров

    svg.innerHTML = ''; //очищаем svg от предыдущих рисунков

    const rects = new Map(); //собираем прямоугольные представления всех блоков
    for (const el of conts) {
        rects.set(el, el.getBoundingClientRect());
    }

    const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
    const scrollY = window.pageYOffset || document.documentElement.scrollTop;

    for (const el of conts) {
        const block = el.parentElement;
        if (!block || block.classList.contains('container-collapsed')) continue; //если элемент в коллапсированном блоке - не рисуем линии для него

        const seq = block.querySelector(':scope > .container-sequence'); //находим первый sequence
        if (!seq || !seq.children.length) continue;

        const r = rects.get(el);
        if (!r) continue;
        if (r.width === 0 && r.height === 0) continue; // родитель скрыт/невидим → не рисуем

        const cy = r.top + scrollY + r.height / 2; //центр родительского блока по вертикале
        const cx = r.right + scrollX; //центр родительского блока по горизонтали
        const vx = cx + GAP; // место проведения вертикальной линии по горизонтали

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
            //Получаем самую высокую и низкую точку для рисования вертикальной линии
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

        if (minY < cy || maxY > cy) {
            pathData += `M${vx},${minY}L${vx},${maxY}`;
        }

        // Горизонтальные ветви к детям
        for (let i = 0; i < kidsData.length; i++) {
            const { kx, ky } = kidsData[i];

            const isTop = (ky === minY && ky < cy);
            const isBottom = (ky === maxY && ky > cy);

            pathData += `M${vx},${ky}L${kx},${ky}`;
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

function copyTextToClipboard(text) {
    // Проверяем, есть ли текст для копирования
    if (!text || typeof text !== 'string') {
        console.error('Неверный текст для копирования');
        return Promise.resolve(false);
    }

    // Если доступен современный Clipboard API, используем его
    if (navigator.clipboard && window.isSecureContext) {
        return navigator.clipboard.writeText(text)
            .then(() => {
            console.log('Скопировано через Clipboard API');
            return true;
        })
            .catch(err => {
            console.warn('Clipboard API не сработал:', err);
            return copyViaExecCommand(text);
        });
    } else {
        // Используем старый метод
        return Promise.resolve(copyViaExecCommand(text));
    }
}

fitSvgToRoot();