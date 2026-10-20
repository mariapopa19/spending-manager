package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;

public record MerchantTotalResponse(String merchant, BigDecimal total, long count) {}
