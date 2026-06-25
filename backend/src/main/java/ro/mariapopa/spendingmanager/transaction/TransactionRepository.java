package ro.mariapopa.spendingmanager.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @EntityGraph(attributePaths = {"category", "person"})
    Page<Transaction> findAll(Pageable pageable);
    Page<Transaction> findAllByPersonId(Long personId, Pageable pageable);
    boolean existsByImportHash(String importHash);
}
