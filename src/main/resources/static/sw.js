// sw.js
self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(clients.claim());
});

// 不缓存任何请求，直接走网络
self.addEventListener('fetch', (event) => {
  return;
});
