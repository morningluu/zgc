// sw.js
const CACHE_NAME = 'my-pwa-v1';

// 可选：预缓存一个离线页面，使离线体验更好
const OFFLINE_URL = '/offline.html';

self.addEventListener('install', (event) => {
  self.skipWaiting();
  // 如果需要缓存离线页面，可取消下面注释
  /*
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.add(OFFLINE_URL);
    })
  );
  */
});

self.addEventListener('activate', (event) => {
  event.waitUntil(clients.claim());
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    fetch(event.request).catch(() => {
      // 如果请求的是导航页面（HTML），返回离线页；否则返回简单错误
      if (event.request.mode === 'navigate') {
        return caches.match(OFFLINE_URL) || new Response('You are offline', { status: 200 });
      }
      return new Response('Offline', { status: 200 });
    })
  );
});
