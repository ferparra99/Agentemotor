package com.agentemotor.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerCustomConfig {

    @Bean
    public SwaggerIndexPageTransformer swaggerIndexTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        return new SwaggerIndexPageTransformer(
                swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider) {
            @Override
            public Resource transform(HttpServletRequest request, Resource resource,
                                      ResourceTransformerChain transformerChain) throws IOException {
                resource = super.transform(request, resource, transformerChain);
                if (resource.getFilename() != null && resource.getFilename().contains("swagger-initializer")) {
                    String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    String script = "const customScript = document.createElement('script');" +
                            "customScript.src = '/swagger-custom.js';" +
                            "document.head.appendChild(customScript);";
                    content = content.replace("window.ui = SwaggerUIBundle({",
                            script + "window.ui = SwaggerUIBundle({");
                    return new TransformedResource(resource, content.getBytes(StandardCharsets.UTF_8));
                }
                return resource;
            }
        };
    }
}
