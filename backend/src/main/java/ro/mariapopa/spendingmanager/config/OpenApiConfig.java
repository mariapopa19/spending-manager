package ro.mariapopa.spendingmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spending Manager API")
                        .description("REST API for managing personal income and expenses.")
                        .version("v0"));
    }

    // prefills month/from/to in swagger with the current month
    @Bean
    public OpenApiCustomizer currentMonthExample() {
        return openApi -> openApi.getPaths()
                .values()
                .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    if (operation.getParameters() == null) return;
                    String now = YearMonth.now(ZoneId.of("Europe/Bucharest")).toString();
                    operation.getParameters().stream()
                            .filter(predicate -> List.of("month", "from", "to").contains(predicate.getName()))
                            .forEach(action -> action.setExample(now));
                }));
    }
}
