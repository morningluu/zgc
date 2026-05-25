package com.example.zgc.service;

import com.example.zgc.model.User;
import com.example.zgc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(String username, String password) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        userRepository.save(user);
        return true;
    }
}