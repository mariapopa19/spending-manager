package ro.mariapopa.spendingmanager.category;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category-rules")
@Tag(name = "Category rule")
public class CategoryRuleController {
  private final CategoryRuleService ruleService;

  public CategoryRuleController(CategoryRuleService ruleService) {
    this.ruleService = ruleService;
  }

  @GetMapping
  public List<CategoryRuleResponse> findAll() {
    return ruleService.findAll();
  }

  @GetMapping("/{id}")
  public CategoryRuleResponse findById(@PathVariable Long id) {
    return ruleService.findById(id);
  }

  @PostMapping
  public ResponseEntity<CategoryRuleResponse> create(
      @Valid @RequestBody CategoryRuleRequest request) {
    CategoryRuleResponse created = ruleService.create(request);
    return ResponseEntity.created(URI.create("/api/category-rules/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public CategoryRuleResponse update(
      @PathVariable Long id, @Valid @RequestBody CategoryRuleRequest request) {
    return ruleService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    ruleService.delete(id);
  }
}
