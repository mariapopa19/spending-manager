package ro.mariapopa.spendingmanager.person;

import jakarta.validation.constraints.NotBlank;

public record PersonRequest(@NotBlank String name) {
}
