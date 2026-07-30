package ro.mariapopa.spendingmanager.statementimport;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ImportPreviewRow(
    LocalDate date, BigDecimal amount, String currency, String description, ImportStatus status) {}
