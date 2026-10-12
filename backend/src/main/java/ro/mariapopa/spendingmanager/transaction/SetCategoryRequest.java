package ro.mariapopa.spendingmanager.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetCategoryRequest(@NotNull Long categoryId, @Valid RulePattern createRule) {
  public record RulePattern(@NotBlank @Size(min = 3, max = 255) String pattern) {
    public RulePattern {
      pattern = pattern == null ? null : pattern.strip();
    }
  }
}
