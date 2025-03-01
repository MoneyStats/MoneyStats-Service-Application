package com.giova.service.moneystats.config;

import com.giova.service.moneystats.authentication.dto.UserData;
import io.github.giovannilamarmora.utils.config.OpenAPIConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Paths;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "io.github.giovannilamarmora.utils")
@Configuration
@EnableScheduling
@EnableCaching
@OpenAPIDefinition(
    info = @Info(title = "MoneyStats Swagger", version = "1.0.0"),
    security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)})
// servers = {@Server(url = "https://myserver1.com"), @Server(url = "https://myserver2.com")})
@SecurityScheme(
    type = SecuritySchemeType.APIKEY,
    name = HttpHeaders.AUTHORIZATION,
    in = SecuritySchemeIn.HEADER)
public class AppConfig {

  @Bean
  public UserData userInfo() {
    return new UserData();
  }

  @Bean
  public OpenApiCustomizer applyStandardOpenAPIModifications() {
    return openApi -> {
      Paths paths = new Paths();
      openApi.getPaths().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(
              entry ->
                  paths.addPathItem(
                      entry.getKey(),
                      OpenAPIConfig.addJSONExamplesOnResource(entry.getValue(), AppConfig.class)));
      openApi.setPaths(paths);
    };
  }
}
