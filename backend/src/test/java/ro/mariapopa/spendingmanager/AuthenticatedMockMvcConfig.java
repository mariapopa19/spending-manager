package ro.mariapopa.spendingmanager;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class AuthenticatedMockMvcConfig {
    @Bean
    MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
        return builder -> builder.defaultRequest(get("/").with(user("maria")).with(csrf()));
    }
}
