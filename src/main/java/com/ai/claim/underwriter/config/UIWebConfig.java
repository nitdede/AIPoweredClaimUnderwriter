package com.ai.claim.underwriter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UIWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve CSS files - try classpath first (for JAR/Docker), then file system (for local dev)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/", "file:src/main/ui/css/")
                .setCachePeriod(0); // Disable caching for development

        // Serve JavaScript files from node/src directory
        registry.addResourceHandler("/node/src/**")
                .addResourceLocations("classpath:/static/node/src/", "file:src/main/ui/node/src/")
                .setCachePeriod(0); // Disable caching for development

        // Legacy support for /js/ paths
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/node/src/", "file:src/main/ui/node/src/")
                .setCachePeriod(0); // Disable caching for development

        // Serve HTML files - try classpath first (for JAR/Docker), then file system (for local dev)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "file:src/main/ui/pages/")
                .setCachePeriod(0); // Disable caching for development
    }
}