package ro.mariapopa.spendingmanager;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // same major version as docker-compose.yml, so tests and dev DB behave the same
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
    }
}
