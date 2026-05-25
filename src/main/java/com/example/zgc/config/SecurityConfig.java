package com.example.zgc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（表单提交需要）
                .csrf(csrf -> csrf.disable())

                // 权限配置
                .authorizeHttpRequests(auth -> auth
                        // 公开页面：所有人都能看
                        .requestMatchers("/", "/gallery", "/growth", "/diary", "/messages").permitAll()
                        // 注册页面（新增）
                        .requestMatchers("/register").permitAll()
                        // 静态资源
                        .requestMatchers("/photos/**", "/uploads/**", "/css/**", "/js/**").permitAll()
                        // 登录页
                        .requestMatchers("/login").permitAll()
                        // 其他所有请求需要登录
                        .anyRequest().authenticated()
                )

                // 登录配置
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // 登出配置
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }
}