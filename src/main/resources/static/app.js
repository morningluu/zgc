// ========== app.js：单历史栈 + 返回键退出确认 ==========

// 1. 注册 Service Worker（用于 PWA 安装）
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js');
}

// 2. 退出确认逻辑
(function() {
  // 保证历史栈有两条记录，这样返回键才会触发 popstate
  if (window.history.length <= 1) {
    history.pushState({ guard: true }, '', location.href);
  }

  function handleBack(e) {
    if (confirm('确定要退出应用吗？')) {
      // 允许退出：移除监听，关闭窗口
      window.removeEventListener('popstate', handleBack);
      window.onbeforeunload = null;
      window.close();
      // 如果 close 无效，强制后退离开
      setTimeout(() => {
        if (!window.closed) {
          history.go(-(history.length - 1));
        }
      }, 200);
    } else {
      // 取消退出：重新推入占位状态，保持长度不变
      history.pushState({ guard: true }, '', location.href);
    }
  }

  window.addEventListener('popstate', handleBack);
})();

// 3. 通用导航函数（用 replace 不留历史）
function navigateTo(url) {
  window._internalNav = true; // 标记内部跳转，beforeunload 不拦截
  location.replace(url);
}

// 4. 拦截所有 <a> 标签点击（内部链接自动替换）
document.addEventListener('click', function(e) {
  const link = e.target.closest('a');
  if (!link) return;
  const href = link.getAttribute('href');
  if (!href) return;

  const isInternal =
    href.startsWith('/') ||
    href.startsWith('.') ||
    href.startsWith(window.location.origin);

  if (isInternal && !href.startsWith('javascript:') && !href.startsWith('#') && href !== '#') {
    e.preventDefault();
    navigateTo(href);
  }
});

// 5. 拦截带有 data-href 属性的元素点击（按钮、div 等）
document.addEventListener('click', function(e) {
  const el = e.target.closest('[data-href]');
  if (el) {
    e.preventDefault();
    navigateTo(el.getAttribute('data-href'));
  }
});

// 6. 退出按钮（如有）
const exitBtn = document.getElementById('exitBtn');
if (exitBtn) {
  exitBtn.addEventListener('click', function() {
    history.back(); // 触发返回键确认流程
  });
}

// 7. beforeunload 二次确认（关闭标签页或刷新时）
window.addEventListener('beforeunload', function(e) {
  if (window._internalNav) {
    window._internalNav = false;
    return;
  }
  e.preventDefault();
  e.returnValue = '';
});
