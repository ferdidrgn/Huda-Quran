(() => {
  const root = document.documentElement;
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const atlas = document.querySelector('#surah-atlas');
  if (atlas) {
    const ns = 'http://www.w3.org/2000/svg';
    for (let i = 0; i < 114; i += 1) {
      const circle = document.createElementNS(ns, 'circle');
      const column = i % 38;
      const row = Math.floor(i / 38);
      circle.setAttribute('cx', String(8 + column * 10.4));
      circle.setAttribute('cy', String(12 + row * 31));
      circle.setAttribute('r', i === 0 ? '3.5' : i % 7 === 0 ? '2.4' : '1.7');
      circle.setAttribute('class', i === 0 ? 'atlas-dot is-current' : 'atlas-dot');
      circle.setAttribute('aria-label', `${i + 1}. sure`);
      atlas.appendChild(circle);
    }
  }
  document.querySelectorAll('.reveal').forEach((element) => {
    if (reduced || !('IntersectionObserver' in window)) { element.classList.add('is-visible'); return; }
    const observer = new IntersectionObserver((entries, handle) => entries.forEach((entry) => { if (entry.isIntersecting) { entry.target.classList.add('is-visible'); handle.unobserve(entry.target); } }), { threshold: 0.12 });
    observer.observe(element);
  });
  const translation = document.querySelector('[data-translation]');
  const readingToggle = document.querySelector('[data-reading-toggle]');
  if (readingToggle && translation) readingToggle.addEventListener('click', () => { const hidden = translation.classList.toggle('is-hidden'); readingToggle.setAttribute('aria-pressed', String(hidden)); readingToggle.textContent = hidden ? 'Türkçe meali göster' : 'Türkçe meali gizle'; });
  const fontToggle = document.querySelector('[data-font-toggle]');
  const desk = document.querySelector('.reading-desk');
  if (fontToggle && desk) fontToggle.addEventListener('click', () => { const large = desk.classList.toggle('is-large'); fontToggle.setAttribute('aria-pressed', String(large)); });
  const menuButton = document.querySelector('[data-menu-toggle]');
  const nav = document.querySelector('.desktop-nav');
  if (menuButton && nav) menuButton.addEventListener('click', () => { const open = nav.classList.toggle('is-open'); menuButton.setAttribute('aria-expanded', String(open)); root.classList.toggle('menu-open', open); });
  document.addEventListener('click', (event) => {
    const anchor = event.target.closest('a[href*="#/"]');
    if (!anchor) return;
    const hashRoute = anchor.getAttribute('href').split('#/')[1].split(/[?#]/)[0];
    const routeAliases = { duas: 'dua', tafsir: 'settings', prayer: '', about: '', home: '' };
    const route = Object.prototype.hasOwnProperty.call(routeAliases, hashRoute) ? routeAliases[hashRoute] : hashRoute;
    event.preventDefault();
    window.location.href = route ? `/?app=1&screen=${encodeURIComponent(route)}` : '/?app=1';
  });
  document.querySelector('[data-copy-link]')?.addEventListener('click', async (event) => { const button = event.currentTarget; try { await navigator.clipboard.writeText(window.location.href); button.textContent = 'Bağlantı kopyalandı ✓'; } catch { button.textContent = 'Bağlantıyı seçip paylaşabilirsin'; } window.setTimeout(() => { button.innerHTML = 'Bağlantıyı paylaş <span>↗</span>'; }, 2200); });
})();
