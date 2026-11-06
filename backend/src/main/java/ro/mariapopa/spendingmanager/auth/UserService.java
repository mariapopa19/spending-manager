package ro.mariapopa.spendingmanager.auth;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.common.DuplicateResourceException;
import ro.mariapopa.spendingmanager.common.InvalidPasswordException;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse create(UserRequest request) {
        String username = request.username().trim();
        if (repository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", username);
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        AppUser saved = repository.save(user);
        return toResponse(saved);
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        AppUser user = repository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash()))
            throw new InvalidPasswordException();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.isEnabled());
    }
}
