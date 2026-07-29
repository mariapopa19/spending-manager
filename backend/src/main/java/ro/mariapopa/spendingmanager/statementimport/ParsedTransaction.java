package ro.mariapopa.spendingmanager.statementimport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedTransaction(
    @NotNull LocalDate date,
    @NotNull BigDecimal amount,
    @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "ISO 4217 code, e.g. RON") String currency,
    @NotBlank String description) {}
