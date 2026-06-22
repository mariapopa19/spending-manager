package ro.mariapopa.spendingmanager.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String description,
        String merchant,
        Long categoryId,
        @NotNull Long personId,
        @NotBlank String source
        ) {
}
