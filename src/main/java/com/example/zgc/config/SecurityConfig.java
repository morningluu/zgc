package com.example.zgc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 完全忽略静态资源的安全检查
     * ✅ 新增了 PWA 关键文件的忽略
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/uploads/**",
                        "/photos/**",
                        "/css/**",
                        "/js/**",
                        // ✅ PWA 关键文件：manifest、Service Worker、app.js
                        "/manifest.json",
                        "/sw.js",
                        "/app.js",
                        // ✅ PWA 图标
                        "/xiao.png",
                        "/da.png",
                        // ✅ 过渡页
                        "/splash.html",
                        // ⚠️ 移除 /api/avatar/** 这里不该用 ignoring
                        // （如果需要公开访问，在 filterChain 里 permitAll 就好）
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ 公开页面（无需登录）
                        .requestMatchers(
                                "/",
                                "/gallery",
                                "/growth",
                                "/diary",
                                "/messages",
                                "/register",
                                "/login",
                                // ✅ 过渡页和 PWA 启动参数
                                "/splash.html"
                        ).permitAll()
                        // ✅ 公开 API（包括头像接口）
                        .requestMatchers("/api/avatar/**").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
