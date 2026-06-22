package ro.mariapopa.spendingmanager.category;

import java.math.BigDecimal;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        BigDecimal monthlyBudget
) {
}
