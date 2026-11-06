package ro.mariapopa.spendingmanager.auth;

public record UserResponse(Long id, String username, UserRole role, boolean enabled) {}
