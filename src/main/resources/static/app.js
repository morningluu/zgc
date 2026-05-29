// ========== app.js：开屏动画控制 + Service Worker + 返回键退出 ==========

// 0. 开屏动画控制（在最前面执行）
(function() {
  var path = window.location.pathname;
  var isSplashPage = (path === '/splash.html' || path === '/splash');

  // 如果是开屏动画页本身，直接放行
  if (isSplashPage) return;

  // ===== 如果是通过 navigateTo 跳转过来的（内部导航），绝不播放动画 =====
  if (window.__internalNav) {
    window.__internalNav = false;
    return;
  }

  // 只有首页才触发开屏动画
  var isHomePage = (path === '/' || path === '');
  if (!isHomePage) return;

  // 检查是否需要播放动画（sessionStorage 在每次浏览器新会话时清空）
  if (!sessionStorage.getItem('splashPlayed')) {
    sessionStorage.setItem('splashPlayed', 'true');

    // 构建目标 URL：保留原始查询参数，追加 standalone 标记
    var targetUrl = '/';
    var currentSearch = window.location.search;

    // 检测是否是 PWA standalone 模式
    var isStandalone = window.matchMedia('(display-mode: standalone)').matches
      || window.navigator.standalone
      || window.location.search.includes('standalone=true');

    if (currentSearch) {
      // 已有查询参数，追加 standalone
      if (!currentSearch.includes('standalone=')) {
        targetUrl = currentSearch + '&standalone=true';
      } else {
        targetUrl = currentSearch;
      }
    } else {
      targetUrl = '/?standalone=true';
    }

    sessionStorage.setItem('splashTarget', targetUrl);
    window.location.replace('/splash.html');
  }
})();

// 1. 注册 Service Worker（页面加载完成后注册）
if ('serviceWorker' in navigator) {
  window.addEventListener('load', function() {
    navigator.serviceWorker.register('/sw.js').then(function(reg) {
      console.log('✅ Service Worker 注册成功，范围：', reg.scope);
    }).catch(function(err) {
      console.log('❌ Service Worker 注册失败：', err);
    });
  });
}

// 2. 退出确认逻辑（通过 popstate + confirm，不拦截 beforeunload）
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
  window.__internalNav = true;   // 标记为内部导航
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

// 6. 退出按钮（如有）
var exitBtn = document.getElementById('exitBtn');
if (exitBtn) {
  exitBtn.addEventListener('click', function() {
    history.back();
  });
}

// 表单提交时不拦截退出
document.addEventListener('submit', function(e) {
  window._internalNav = true;
});
