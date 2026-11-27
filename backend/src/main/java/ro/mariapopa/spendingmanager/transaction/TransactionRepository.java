package ro.mariapopa.spendingmanager.transaction;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @EntityGraph(attributePaths = {"category", "person"})
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    boolean existsByImportHash(String importHash);

    List<Transaction> findByCategoryIsNull();
}
