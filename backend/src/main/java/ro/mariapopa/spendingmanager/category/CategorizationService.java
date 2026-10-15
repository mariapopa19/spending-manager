package ro.mariapopa.spendingmanager.category;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
        if (description == null) return Optional.empty();

        String haystack = normalize(description.toLowerCase(Locale.ROOT));
        return rules.stream()
                .filter(rule -> haystack.contains(normalize(rule.getPattern().trim())))
                .max(Comparator.comparingInt((CategoryRule rule) ->
                                normalize(rule.getPattern().trim()).length())
                        .thenComparing(CategoryRule::getId, Comparator.reverseOrder()))
                .map(CategoryRule::getCategory);
    }

    static String normalize(String text) {
        String decomposed = Normalizer.normalize(text.strip(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
