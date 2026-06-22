package ro.mariapopa.spendingmanager.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CategoryRequest(
        @NotBlank String name,
        String color,
        @PositiveOrZero BigDecimal monthlyBudget) {
}
