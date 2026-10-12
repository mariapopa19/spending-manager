package ro.mariapopa.spendingmanager.category;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.common.DuplicateResourceException;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;

@Service
public class CategoryRuleService {
  private final CategoryRuleRepository ruleRepository;
  private final CategoryRepository categoryRepository;

  public CategoryRuleService(
      CategoryRuleRepository ruleRepository, CategoryRepository categoryRepository) {
    this.ruleRepository = ruleRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional(readOnly = true)
  public List<CategoryRuleResponse> findAll() {
    return ruleRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public CategoryRuleResponse findById(Long id) {
    CategoryRule rule =
        ruleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rule", id));
    return toResponse(rule);
  }

  public CategoryRuleResponse create(CategoryRuleRequest request) {
    String pattern = request.pattern().trim();
    if (ruleRepository.existsByPatternIgnoreCase(pattern)) {
      throw new DuplicateResourceException("Rule", pattern);
    }
    Category category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
    CategoryRule rule = new CategoryRule();
    rule.setPattern(pattern);
    rule.setCategory(category);
    return toResponse(ruleRepository.save(rule));
  }

  @Transactional
  public CategoryRuleResponse update(Long id, CategoryRuleRequest request) {
    CategoryRule rule =
        ruleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rule", id));
    Category category =
        categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
    rule.setPattern(request.pattern());
    rule.setCategory(category);
    return toResponse(rule);
  }

  public void delete(Long id) {
    if (!ruleRepository.existsById(id)) {
      throw new ResourceNotFoundException("Rule", id);
    }
    ruleRepository.deleteById(id);
  }

  private CategoryRuleResponse toResponse(CategoryRule categoryRule) {
    Category category = categoryRule.getCategory();
    return new CategoryRuleResponse(
        categoryRule.getId(),
        categoryRule.getPattern(),
        category != null ? category.getId() : null,
        category != null ? category.getName() : null);
  }
}
