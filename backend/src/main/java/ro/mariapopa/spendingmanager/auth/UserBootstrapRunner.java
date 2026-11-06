package ro.mariapopa.spendingmanager.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserBootstrapRunner implements ApplicationRunner {
    private final AppUserRepository repository;
    private final String username;
    private final String passwordHash;
    private static final Logger log = LoggerFactory.getLogger(UserBootstrapRunner.class);

    public UserBootstrapRunner(
            AppUserRepository repository,
            @Value("${app.auth.username}") String username,
            @Value("${app.auth.password-hash}") String passwordHash) {
        this.repository = repository;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordHash);
        admin.setRole(UserRole.ADMIN);
        repository.save(admin);
        log.info("Created initial admin user '{}'", username);
    }
}
