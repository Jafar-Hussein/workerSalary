package com.example.examen.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Indikerar att denna klass innehåller konfigurationsinställningar för applikationen
public class ApplicationConfig {

    // Definierar en Bean för ModelMapper som kan användas i hela applikationen
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper(); // Skapar och returnerar en instans av ModelMapper
    }
}
