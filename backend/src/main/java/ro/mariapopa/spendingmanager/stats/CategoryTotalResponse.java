package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;

public record CategoryTotalResponse(
        Long categoryId, String name, String color, BigDecimal estimated, BigDecimal actual) {}
