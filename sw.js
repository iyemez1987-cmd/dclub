// D Club: çevrimdışı açılış için basit önbellek. Supabase istekleri hiç önbelleğe alınmaz.
const SURUM = 'dclub-v1';
const KABUK = ['./', './index.html', './manifest.webmanifest', './icon-192.png', './icon-512.png', './apple-touch-icon.png'];
const DIS = ['https://cdnjs.cloudflare.com/ajax/libs/qrcode-generator/1.4.4/qrcode.min.js'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(SURUM).then((c) => c.addAll(KABUK).then(() => Promise.all(DIS.map((u) => c.add(u).catch(() => {}))))).then(() => self.skipWaiting()));
});
self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((k) => Promise.all(k.filter((x) => x !== SURUM).map((x) => caches.delete(x)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', (e) => {
  const r = e.request;
  if (r.method !== 'GET' || r.url.includes('supabase.co')) return;
  if (r.mode === 'navigate') {
    // sayfa: önce ağ (güncel sürüm), yoksa önbellek
    e.respondWith(fetch(r).then((y) => { const k = y.clone(); caches.open(SURUM).then((c) => c.put('./index.html', k)); return y; })
      .catch(() => caches.match('./index.html')));
    return;
  }
  e.respondWith(caches.match(r).then((v) => v || fetch(r).then((y) => {
    if (y.ok && (r.url.startsWith(self.location.origin) || r.url.includes('fonts.g') || r.url.includes('cdnjs'))) {
      const k = y.clone(); caches.open(SURUM).then((c) => c.put(r, k));
    }
    return y;
  })));
});
