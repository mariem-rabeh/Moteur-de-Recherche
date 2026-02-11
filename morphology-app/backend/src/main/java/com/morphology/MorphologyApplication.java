package com.morphology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MorphologyApplication {
    
    public static void main(String[] args) {
        // Configurer UTF-8 pour l'application
        System.setProperty("file.encoding", "UTF-8");
        SpringApplication.run(MorphologyApplication.class, args);
    }
}