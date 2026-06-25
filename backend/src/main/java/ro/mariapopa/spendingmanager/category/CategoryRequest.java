package ro.mariapopa.spendingmanager.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CategoryRequest(
        @NotBlank String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be a hex color like #64B5F6") String color,
        @PositiveOrZero BigDecimal monthlyBudget) {
}
