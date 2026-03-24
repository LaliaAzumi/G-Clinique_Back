package com.erp.clinique.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cette ligne lie l'URL /pdf_ordonnances/** au dossier physique /app/pdf_ordonnances/ dans Docker
        registry.addResourceHandler("/pdf_ordonnances/**")
                .addResourceLocations("file:/app/pdf_ordonnances/");
    }
}