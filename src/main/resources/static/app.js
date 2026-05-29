// ========== app.js：开屏动画控制 + Service Worker + 返回键退出 ==========

// 0. 开屏动画控制（在最前面执行）
(function() {
  // 当前页面是否是开屏动画页
  var path = window.location.pathname;
  var isSplashPage = (path === '/splash.html' || path === '/splash');

  // 如果不是开屏动画页，检查是否需要播放动画
  if (!isSplashPage) {
    // sessionStorage 中没有标记，说明是新会话，需要播放动画
    if (!sessionStorage.getItem('splashPlayed')) {
      // 保存当前要去的目标页面路径
      var targetPath = window.location.pathname + window.location.search;
      if (targetPath === '/' || targetPath === '') {
        targetPath = '/?standalone=true';
      }
      sessionStorage.setItem('splashTarget', targetPath);
      // 标记动画已播放，防止无限循环
      sessionStorage.setItem('splashPlayed', 'true');
      // 跳转到开屏动画页
      window.location.replace('/splash.html');
      return; // 停止执行后续所有代码
    }
    // 已经看过动画，继续正常加载页面
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
  window._internalNav = true;
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
