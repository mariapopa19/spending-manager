package ro.mariapopa.spendingmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

    @Bean
    public OpenApiCustomizer loginEndpoint() {
        return openApi -> {
            Schema<?> credentials = new ObjectSchema();
            credentials.addProperty("username", new StringSchema().example("maria"));
            credentials.addProperty("password", new StringSchema().format("password"));
            credentials.required(List.of("username", "password"));
            Operation login = new Operation()
                    .addTagsItem("Auth")
                    .summary("Log in - sets the JSESSIONID session cookie, empty body")
                    .requestBody(new RequestBody()
                            .required(true)
                            .content(new Content()
                                    .addMediaType(
                                            "application/x-www-form-urlencoded", new MediaType().schema(credentials))))
                    .responses(new ApiResponses()
                            .addApiResponse("200", new ApiResponse().description("Logged in"))
                            .addApiResponse("401", new ApiResponse().description("Bad credentials")));
            openApi.getPaths().addPathItem("/api/auth/login", new PathItem().post(login));
        };
    }

    @Bean
    public OpenApiCustomizer logoutEndpoint() {
        return openApi -> {
            Operation logout = new Operation()
                    .addTagsItem("Auth")
                    .summary("Log out - invalidates the session")
                    .responses(new ApiResponses().addApiResponse("204", new ApiResponse().description("Logged out")));
            openApi.getPaths().addPathItem("/api/auth/logout", new PathItem().post(logout));
        };
    }
}
