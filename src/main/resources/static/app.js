// ========== app.js：开屏动画控制 + Service Worker + 返回键退出 ==========

// 0. 开屏动画控制（在最前面执行）
(function() {
  var path = window.location.pathname;
  var isSplashPage = (path === '/splash.html' || path === '/splash');

  // 开屏动画页本身直接放行
  if (isSplashPage) return;

  // 内部导航跳转过来的，绝不播放动画
  if (sessionStorage.getItem('skipSplash') === 'true') {
    sessionStorage.removeItem('skipSplash');
    return;
  }

  // 只有首页才触发开屏动画
  var isHomePage = (path === '/' || path === '');
  if (!isHomePage) return;

  // URL 自带 loaded 或 standalone 参数 → 说明刚从 splash 跳过来 → 跳过
  var params = new URLSearchParams(window.location.search);
  if (params.get('loaded') === 'true' || params.get('standalone') === 'true') {
    return;
  }

  // 检查是否需要播放开屏动画（每个会话只播一次）
  if (!sessionStorage.getItem('splashPlayed')) {
    sessionStorage.setItem('splashPlayed', 'true');
    // 告诉 splash 页面播完后跳转到哪里
    sessionStorage.setItem('splashTarget', '/?loaded=true');
    window.location.replace('/splash.html');
  }
})();

// 1. 注册 Service Worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function() {
    navigator.serviceWorker.register('/sw.js').then(function(reg) {
      // Service Worker registered
    }).catch(function(err) {
      // Service Worker registration failed
    });
  });
}

// 2. 退出确认逻辑
(function() {
  if (window.history.length <= 1) {
    history.pushState({ guard: true }, '', location.href);
  }

  function handleBack(e) {
    if (confirm('确定要退出应用吗？')) {
      window.removeEventListener('popstate', handleBack);
      window.close();
      setTimeout(function() {
        if (!window.closed) {
          history.go(-(history.length - 1));
        }
      }, 200);
    } else {
      history.pushState({ guard: true }, '', location.href);
    }
  }

  window.addEventListener('popstate', handleBack);
})();

// 3. 通用导航函数
function navigateTo(url) {
  sessionStorage.setItem('skipSplash', 'true');
  // 内部跳转到首页时带上 loaded=true，避免服务器返回启动动画页
  if (url === '/' || url === '' || url === '/index' || url === '/index.html') {
    url = '/?loaded=true';
  }
  location.replace(url);
}

// 4. 拦截 <a> 标签内部跳转
document.addEventListener('click', function(e) {
  var link = e.target.closest('a');
  if (!link) return;
  var href = link.getAttribute('href');
  if (!href) return;

  var isInternal =
    href.startsWith('/') ||
    href.startsWith('.') ||
    href.startsWith(window.location.origin);

  if (isInternal && !href.startsWith('javascript:') && !href.startsWith('#') && href !== '#') {
    e.preventDefault();
    navigateTo(href);
  }
});

// 5. 拦截 data-href 元素
document.addEventListener('click', function(e) {
  var el = e.target.closest('[data-href]');
  if (el) {
    e.preventDefault();
    navigateTo(el.getAttribute('data-href'));
  }
});

// 6. 退出按钮
var exitBtn = document.getElementById('exitBtn');
if (exitBtn) {
  exitBtn.addEventListener('click', function() {
    history.back();
  });
}

// 表单提交时不拦截退出
document.addEventListener('submit', function(e) {
  sessionStorage.setItem('skipSplash', 'true');
});
