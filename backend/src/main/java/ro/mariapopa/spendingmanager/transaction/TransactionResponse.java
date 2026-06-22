package ro.mariapopa.spendingmanager.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        LocalDate date,
        BigDecimal amount,
        String currency,
        String description,
        String merchant,
        Long categoryId,
        String categoryName,
        String categoryColor,
        Long personId,
        String personName,
        String source,
        String importHash,
        Instant createdAt
) {
}
