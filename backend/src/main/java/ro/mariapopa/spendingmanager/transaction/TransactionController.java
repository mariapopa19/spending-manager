package ro.mariapopa.spendingmanager.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction")
public class TransactionController {
  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @GetMapping
  public Page<TransactionResponse> findAll(
      @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
      @RequestParam(defaultValue = "false") boolean uncategorized) {

    return transactionService.findAll(pageable, uncategorized);
  }

  @GetMapping("/{id}")
  public TransactionResponse findById(@PathVariable Long id) {
    return transactionService.findById(id);
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(
      @Valid @RequestBody TransactionRequest request) {
    TransactionResponse created = transactionService.create(request);
    return ResponseEntity.created(URI.create("/api/transactions/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  public TransactionResponse update(
      @PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
    return transactionService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    transactionService.delete(id);
  }

  @PostMapping("/apply-rules")
  public ApplyRulesResponse applyCategoryRules() {
    return new ApplyRulesResponse(transactionService.applyToUncategorized());
  }

  @PatchMapping("/{id}/category")
  @Operation(
      summary = "Set a transaction's category, optionally creating a rule",
      description =
          """
                  Assigns the given category to this transaction.

                  If `createRule` is provided, a categorization rule is also created that maps
                  `createRule.pattern` to the same category. Matching is case-insensitive
                  and uses "description contains pattern".

                  The new rule is **not** applied to other uncategorized transactions by this
                  request. Use `POST /api/transactions/apply-rules` (the Re-apply button) for that.

                  The request is atomic. If the rule cannot be created (for example, a rule with
                  the same pattern already exists), the category change is rolled back as well.

                  - **200**: category set (and rule created, if requested)
                  - **400**: validation failed (missing `categoryId`, pattern blank or shorter than 3 characters)
                  - **404**: transaction or category not found
                  - **409**: a rule with this pattern already exists; nothing was changed
                  """)
  public TransactionResponse setCategory(
      @PathVariable Long id, @Valid @RequestBody SetCategoryRequest request) {
    return transactionService.setCategory(id, request);
  }
}
