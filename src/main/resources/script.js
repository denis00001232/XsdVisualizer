const GAP = 40;
const STROKE = '#333';
const WIDTH = 2;
const RADIUS = 8; // Радиус закругления углов
const conts = Array.from(document.querySelectorAll('.container'));
let blockDraw = false
let svgSizeSet = false; // Флаг, что размер уже установлен

const svg = document.getElementById('svgLayer');
const rootBlock = document.querySelector('.container-block-root');

/* ---- устанавливаем размер SVG один раз при инициализации ---- */
function fitSvgToRoot() {
    if (!rootBlock || !svg) return;

    // Если размер уже установлен, только перерисовываем
    if (svgSizeSet) {
        drawLines();
        return;
    }

    // Временно разворачиваем всё для получения максимального размера
    const collapsedBlocks = [];
    document.querySelectorAll('.container-collapsed').forEach(block => {
        collapsedBlocks.push(block);
        block.classList.remove('container-collapsed');
    });

    // Небольшая задержка для применения стилей
    requestAnimationFrame(() => {
        const body = document.body;
        const html = document.documentElement;

        // Получаем максимальный размер документа
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

        // Устанавливаем фиксированный размер
        svg.style.width = `${width + gap2}px`;
        svg.style.height = `${height + gap2}px`;
        svg.style.position = 'absolute';
        svg.style.top = '0';
        svg.style.left = '0';
        svg.style.pointerEvents = 'none';

        // Устанавливаем viewBox для корректного масштабирования
        svg.setAttribute('width', width + gap2);
        svg.setAttribute('height', height + gap2);
        svg.setAttribute('viewBox', `0 0 ${width + gap2} ${height + gap2}`);
        svg.setAttribute('preserveAspectRatio', 'xMinYMin meet');

        svgSizeSet = true;

        // Возвращаем collapsed состояние обратно
        collapsedBlocks.forEach(block => {
            block.classList.add('container-collapsed');
        });

        // Рисуем линии после восстановления состояния
        drawLines();
    });
}

function setupContainerInfo() {

}

function copyPath(container) {
    const path = []
    while (true) {

    }
}

/* ---- ГЛАВНАЯ функция отрисовки ---- */
function drawLines() {
    if (blockDraw) {
        return
    }
    console.log("Рисую линию")
    if (!svg || !conts.length) return;

    // Очищаем SVG
    svg.innerHTML = '';

    // Получаем все контейнеры и их позиции (один проход)
    const rects = new Map();
    for (const el of conts) {
        rects.set(el, el.getBoundingClientRect());
    }

    const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
    const scrollY = window.pageYOffset || document.documentElement.scrollTop;

    // Строим одну большую строку path для всех линий
    let pathData = '';

    for (const el of conts) {
        const block = el.parentElement;
        if (!block || block.classList.contains('container-collapsed')) continue;

        const seq = block.querySelector(':scope > .container-sequence');
        if (!seq || !seq.children.length) continue;

        const r = rects.get(el);
        if (!r) continue;

        const cy = r.top + scrollY + r.height / 2;
        const cx = r.right + scrollX;
        const vx = cx + GAP;

        // Горизонтальная линия от родителя
        pathData += `M${cx},${cy}L${vx},${cy}`;

        let minY = cy;
        let maxY = cy;

        // Собираем данные по детям за один проход
        const kidsData = [];

        for (const childBlock of seq.children) {
            const kid = childBlock.querySelector(':scope > .container');
            if (!kid) continue;

            const kr = rects.get(kid);
            if (!kr) continue;

            const ky = kr.top + scrollY + kr.height / 2;
            const kx = kr.left + scrollX;

            kidsData.push({ kx, ky });

            if (ky < minY) minY = ky;
            if (ky > maxY) maxY = ky;
        }

        if (!kidsData.length) continue;

        // Случай с одним ребенком
        if (kidsData.length === 1) {
            const { kx, ky } = kidsData[0];
            // Просто прямая линия
            pathData += `M${vx},${ky}L${kx},${ky}`;
            continue;
        }

        // Случай с несколькими детьми
        // Вертикальная линия с учетом закругления только на краях
        if (minY < cy) {
            pathData += `M${vx},${minY + RADIUS}L${vx},${maxY - RADIUS}`;
        } else if (maxY > cy) {
            pathData += `M${vx},${minY + RADIUS}L${vx},${maxY - RADIUS}`;
        }

        // Горизонтальные линии к детям с закруглениями только для крайних
        for (let i = 0; i < kidsData.length; i++) {
            const { kx, ky } = kidsData[i];

            // Проверяем, является ли элемент самым верхним или самым нижним
            const isTop = (ky === minY && ky < cy);
            const isBottom = (ky === maxY && ky > cy);

            if (isTop) {
                // Самый верхний элемент - закругление вниз
                pathData += `M${vx},${ky + RADIUS}Q${vx},${ky} ${vx + RADIUS},${ky}L${kx},${ky}`;
            } else if (isBottom) {
                // Самый нижний элемент - закругление вверх
                pathData += `M${vx},${ky - RADIUS}Q${vx},${ky} ${vx + RADIUS},${ky}L${kx},${ky}`;
            } else {
                // Средние элементы - прямая линия от вертикали
                pathData += `M${vx},${ky}L${kx},${ky}`;
            }
        }
    }

    // Создаём path элемент, если есть что рисовать
    if (pathData) {
        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('d', pathData);
        path.setAttribute('stroke', STROKE);
        path.setAttribute('stroke-width', WIDTH);
        path.setAttribute('stroke-linejoin', 'round');
        path.setAttribute('stroke-linecap', 'round');
        path.setAttribute('fill', 'none');
        path.setAttribute('vector-effect', 'non-scaling-stroke'); // Толщина линии не масштабируется
        svg.appendChild(path);
    }
}

/* ---- сворачивания/разворачивания ---- */
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
    blockDraw = true
    document.querySelectorAll('.button').forEach(showElement);
    blockDraw = false
    drawLines();
}

function hideAll() {
    blockDraw = true
    document.querySelectorAll('.button').forEach(hideElement);
    blockDraw = false
    drawLines();
}

/* ---- события ---- */
let resizeTimeout;
window.addEventListener(
    'resize',
    () => {
        // Используем debounce для избежания множественных перерисовок
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            drawLines();
        }, 100);
    },
    { passive: true }
);

// Отслеживаем изменение zoom
if (window.visualViewport) {
    let zoomTimeout;
    window.visualViewport.addEventListener('resize', () => {
        clearTimeout(zoomTimeout);
        zoomTimeout = setTimeout(() => {
            drawLines();
        }, 100);
    });
}

/* ---- инициализация ---- */
// Устанавливаем размер SVG при загрузке
fitSvgToRoot();