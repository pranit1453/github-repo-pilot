package com.pranit.github.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Repo Pilot - GitHub Repository Chat",
                description = "Rest API for Repo Pilot: GitHub repository ingestion, vector embeddings and conversational question answering with RAG.",
                version = "1.0.0",
                summary = "Repo Pilot API",
                contact = @Contact(
                        name = "Pranit Bhangale",
                        url = "https://pranitbhangale.vercel.app",
                        email = "pranitbhangale1453@gmail.com"
                )
        )
)
@SecurityScheme(
        name = "accessToken",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "access_token",
        description = "JWT access token stored in HttpOnly cookie"
)
public class SwaggerConfig {
}