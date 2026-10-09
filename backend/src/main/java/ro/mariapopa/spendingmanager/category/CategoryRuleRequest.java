package ro.mariapopa.spendingmanager.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRuleRequest(
    @NotBlank @Size(min = 3, max = 255) String pattern, @NotNull Long categoryId) {}
