package ro.mariapopa.spendingmanager.category;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategorizationService {
  private final CategoryRuleRepository ruleRepository;

  public CategorizationService(CategoryRuleRepository ruleRepository) {
    this.ruleRepository = ruleRepository;
  }

  @Transactional(readOnly = true)
  public Function<String, Optional<Category>> matcher() {
    List<CategoryRule> rules = ruleRepository.findAll();
    return description -> match(rules, description);
  }

  static Optional<Category> match(List<CategoryRule> rules, String description) {
    String haystack = description.toLowerCase(Locale.ROOT);
    return rules.stream()
        .filter(rule -> haystack.contains(rule.getPattern().trim().toLowerCase(Locale.ROOT)))
        .max(
            Comparator.comparingInt((CategoryRule rule) -> rule.getPattern().trim().length())
                .thenComparing(CategoryRule::getId, Comparator.reverseOrder()))
        .map(CategoryRule::getCategory);
  }
}
