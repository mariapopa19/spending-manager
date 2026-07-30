package ro.mariapopa.spendingmanager.statementimport;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import ro.mariapopa.spendingmanager.transaction.Source;

public record ImportConfirmRequest(
    @NotNull Source source, @NotNull Long personId, @NotEmpty List<@Valid ImportRowRequest> rows) {}
