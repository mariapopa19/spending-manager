package ro.mariapopa.spendingmanager.category;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {
  boolean existsByPatternIgnoreCase(String pattern);

  @EntityGraph(attributePaths = "category")
  List<CategoryRule> findAll();
}
