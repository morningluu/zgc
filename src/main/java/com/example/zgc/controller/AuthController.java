package com.example.zgc.controller;

import com.example.zgc.service.UserService;  // ← 加这一行
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致");
            return "register";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "密码长度不能少于6位");
            return "register";
        }

        if (username.length() < 4) {
            model.addAttribute("error", "用户名长度不能少于4位");
            return "register";
        }

        boolean success = userService.register(username, password);
        if (!success) {
            model.addAttribute("error", "用户名已存在，请换一个");
            return "register";
        }

        model.addAttribute("success", "注册成功！请登录");
        return "login";
    }
}