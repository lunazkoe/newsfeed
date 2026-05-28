package com.lunazkoe.newsfeed.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev"})
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(infoConfig())
            .servers(serverConfig());
    }

    private Info infoConfig() {
        return new Info()
            .title("NewsFeed API 문서")
            .description("NewsFeed API 문서")
            .version("0.0.1");
    }

    private List<Server> serverConfig() {
        return List.of(
            new Server().url("http://localhost:8080").description("Local Server")
        );
    }

}
