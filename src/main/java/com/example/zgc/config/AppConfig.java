package com.example.zgc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class AppConfig {
    private String dir;
    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
}