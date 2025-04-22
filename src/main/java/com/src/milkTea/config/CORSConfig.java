package com.src.milkTea.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// This class configures CORS (Cross-Origin Resource Sharing) settings for the application.
public class CORSConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://swp-3-w.vercel.app","https://beautiful-unity-production.up.railway.app","https://swp-3-w.vercel.app/",
                        "http://localhost:5173") // config https for Frontend and railway and localhost Frontend
                .allowedHeaders("*")
                .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Methods", "Access-Control-Allow-Headers")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(360000); // 1 hour
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

}