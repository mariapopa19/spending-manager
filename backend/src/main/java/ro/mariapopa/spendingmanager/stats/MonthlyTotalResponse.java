package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;

public record MonthlyTotalResponse(String month, BigDecimal expenses, BigDecimal income) {}
