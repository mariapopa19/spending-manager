package ro.mariapopa.spendingmanager.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findAllByPersonId(Long personId, Pageable pageable);
    boolean existsByImportHash(String importHash);
}
