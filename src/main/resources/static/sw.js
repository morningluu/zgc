// Service Worker - 成长手册 PWA
const CACHE_VERSION = 'zgc-v2';
const STATIC_CACHE = 'zgc-static-' + CACHE_VERSION;
const DYNAMIC_CACHE = 'zgc-dynamic-' + CACHE_VERSION;

// 需要预缓存的静态资源
const STATIC_ASSETS = [
  '/',
  '/splash.html',
  '/login',
  '/manifest.json',
  '/app.js',
  '/xiao.png',
  '/da.png',
];

// 安装：预缓存静态资源
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(STATIC_CACHE).then((cache) => {
      return Promise.allSettled(
        STATIC_ASSETS.map((url) =>
          cache.add(url).catch(() => console.log('预缓存跳过:', url))
        )
      );
    }).then(() => self.skipWaiting())
  );
});

// 激活：清理旧缓存
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys
          .filter((key) => key !== STATIC_CACHE && key !== DYNAMIC_CACHE)
          .map((key) => caches.delete(key))
      );
    }).then(() => clients.claim())
  );
});

// 请求策略
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);

  // 跳过非 HTTP(S) 请求和 Chrome 扩展
  if (!url.protocol.startsWith('http')) return;
  if (url.origin !== self.location.origin) return;

  // POST 请求直接走网络（不缓存）
  if (request.method !== 'GET') return;

  // API / 动态页面：网络优先，失败时读缓存
  if (
    url.pathname.startsWith('/api/') ||
    url.pathname.startsWith('/growth') ||
    url.pathname.startsWith('/gallery') ||
    url.pathname.startsWith('/messages')
  ) {
    event.respondWith(networkFirst(request));
    return;
  }

  // H2 Console 不缓存
  if (url.pathname.startsWith('/h2-console')) return;

  // 静态资源：缓存优先
  event.respondWith(cacheFirst(request));
});

// 缓存优先策略
async function cacheFirst(request) {
  const cached = await caches.match(request);
  if (cached) return cached;
  try {
    const response = await fetch(request);
    const cache = await caches.open(STATIC_CACHE);
    cache.put(request, response.clone());
    return response;
  } catch (e) {
    return new Response('离线', { status: 503 });
  }
}

// 网络优先策略
async function networkFirst(request) {
  try {
    const response = await fetch(request);
    const cache = await caches.open(DYNAMIC_CACHE);
    cache.put(request, response.clone());
    return response;
  } catch (e) {
    const cached = await caches.match(request);
    if (cached) return cached;
    // 最后兜底
    return new Response(
      '<html><body style="text-align:center;padding-top:40vh;font-family:sans-serif;color:#999;">📡 网络不可用<br><small>请连接网络后重试</small></body></html>',
      { status: 200, headers: { 'Content-Type': 'text/html' } }
    );
  }
}
