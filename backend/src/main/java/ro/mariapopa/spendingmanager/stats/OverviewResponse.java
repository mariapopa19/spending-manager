package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;

public record OverviewResponse(
        String month,
        String currency,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal estimated,
        BigDecimal remaining,
        long uncategorizedCount) {}
