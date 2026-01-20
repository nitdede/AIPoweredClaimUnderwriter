package com.ai.claim.underwriter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UIWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve CSS files from the UI css directory
        registry.addResourceHandler("/css/**")
                .addResourceLocations("file:src/main/ui/css/")
                .setCachePeriod(0); // Disable caching for development

        // Serve JS files from the UI assets directory
        // Serve JavaScript files from node/src directory
        registry.addResourceHandler("/node/src/**")
                .addResourceLocations("file:src/main/ui/node/src/")
                .setCachePeriod(0); // Disable caching for development

        // Legacy support for /js/ paths
        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:src/main/ui/node/src/")
                .setCachePeriod(0); // Disable caching for development

        // Serve HTML files from the UI pages directory
        registry.addResourceHandler("/**")
                .addResourceLocations("file:src/main/ui/pages/")
                .setCachePeriod(0); // Disable caching for development
    }
}