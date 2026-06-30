package ro.mariapopa.spendingmanager.person;

import java.time.Instant;

public record PersonResponse(
        Long id,
        String name,
        Integer version,
        String createdBy,
        Instant createdDate,
        String lastModifiedBy,
        Instant lastModifiedDate
) {
}
