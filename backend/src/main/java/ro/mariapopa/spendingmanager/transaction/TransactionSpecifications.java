package ro.mariapopa.spendingmanager.transaction;

import org.springframework.data.jpa.domain.Specification;

public final class TransactionSpecifications {
    private TransactionSpecifications() {}

    static Specification<Transaction> hasPerson(Long personId) {
        if (personId == null) {
            return Specification.unrestricted();
        }

        return (root, query, builder) -> builder.equal(root.get("person").get("id"), personId);
    }

    static Specification<Transaction> isUncategorized(boolean uncategorized) {
        if (!uncategorized) {
            return Specification.unrestricted();
        }
        return (root, query, builder) -> builder.isNull(root.get("category"));
    }

    static Specification<Transaction> matching(TransactionFilter filter) {
        return Specification.allOf(hasPerson(filter.personId()), isUncategorized(filter.uncategorized()));
    }
}
