# 赵高钏的成长手册 - Android App

## 环境准备

1. 安装 **Android Studio**：https://developer.android.com/studio
2. 安装 **Node.js**：已有 v24

## 构建 APK

```bash
# 1. 安装依赖
npm install

# 2. 添加 Android 平台
npx cap add android

# 3. 同步配置
npx cap sync android

# 4. 打开 Android Studio 构建
npx cap open android
```

在 Android Studio 中：**Build → Build Bundle(s) / APK(s) → Build APK(s)**

APK 位于 `android/app/build/outputs/apk/debug/app-debug.apk`

## 配置

`capacitor.config.json` 中的 `server.url` 指向 `https://zgc-production.up.railway.app`。

如果域名更换，修改这个地址后重新 `npx cap sync android`。
