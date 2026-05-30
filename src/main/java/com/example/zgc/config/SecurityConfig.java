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
     * 忽略静态资源的安全检查（仅真正的静态文件）
     * 不建议将页面文件放在这里，因为被忽略的路径无法使用 Spring Security 的其他特性
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/uploads/**",
                        "/photos/**",
                        "/css/**",
                        "/js/**",
                        // PWA 关键文件
                        "/manifest.json",
                        "/sw.js",
                        "/app.js",
                        // PWA 图标
                        "/xiao.png",
                        "/da.png",
                        // PWA 截图（新增）
                        "/screenshot-wide.png",
                        "/screenshot-mobile.png"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 公开页面（无需登录即可访问）
                        .requestMatchers(
                                "/",
                                "/gallery",
                                "/growth",
                                "/messages",
                                "/login",
                                "/splash.html"
                        ).permitAll()
                        // 公开 API（仅 GET 获取头像）
                        .requestMatchers("/api/avatar").permitAll()
                        // 管理员接口需要 ADMIN 角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 所有 POST 操作都需要认证
                        .requestMatchers(request ->
                            "POST".equalsIgnoreCase(request.getMethod())
                        ).authenticated()
                        // 其他请求需要认证
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
