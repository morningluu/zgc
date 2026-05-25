package com.example.zgc.config;

import com.example.zgc.model.User;
import com.example.zgc.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin") == null) {
                User admin = new User("admin", passwordEncoder.encode("123456"), "ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("=== 默认管理员已创建: admin / 123456 ===");
            }
        };
    }
}