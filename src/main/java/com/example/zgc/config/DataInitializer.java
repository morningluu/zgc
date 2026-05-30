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
            String familyPw = passwordEncoder.encode("20170224");

            // 管理员
            if (userRepository.findByUsername("admin") == null) {
                userRepository.save(new User("admin", passwordEncoder.encode("123456"), "ROLE_ADMIN"));
                System.out.println("=== 管理员已创建: admin / 123456 ===");
            }

            // 家人账号（密码统一 20170224）
            String[] family = {"爸爸", "妈妈", "姨妈", "姐姐", "爷爷", "婆婆", "外婆", "钏钏"};
            for (String name : family) {
                if (userRepository.findByUsername(name) == null) {
                    userRepository.save(new User(name, familyPw, "ROLE_USER"));
                    System.out.println("=== 家人账号已创建: " + name + " ===");
                }
            }
        };
    }
}