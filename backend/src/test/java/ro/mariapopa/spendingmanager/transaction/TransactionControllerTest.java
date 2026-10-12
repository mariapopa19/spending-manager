package ro.mariapopa.spendingmanager.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import ro.mariapopa.spendingmanager.category.CategoryResponse;
import ro.mariapopa.spendingmanager.category.CategoryRuleResponse;

@SpringBootTest
@AutoConfigureRestTestClient
public class TransactionControllerTest {
  @Autowired RestTestClient restTestClient;
  private static String BASE_URI = "/api/transactions";

  private RestTestClient.ResponseSpec postTransaction(Object body) {
    return restTestClient
        .post()
        .uri(BASE_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange();
  }

  private TransactionResponse createTransaction(Long categoryId) {
    TransactionRequest request =
        new TransactionRequest(
            LocalDate.of(2026, 6, 1),
            new BigDecimal("-12.50"),
            "RON",
            "uncategorized filter test",
            null,
            categoryId,
            1L,
            Source.REVOLUT);
    return restTestClient
        .post()
        .uri(BASE_URI)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(TransactionResponse.class)
        .returnResult()
        .getResponseBody();
  }

  @Test
  void whenUnknownSource_thenReturn400() {
    // raw String — the typed record CAN'T carry "BANANA" (the enum forbids it at compile time)
    String body =
        """
        {
        "date":"2026-06-01",
        "amount":-12.50,
        "currency":"RON",
         "description":"x",
         "personId":1,
         "source":"BANANA"
         }
        """;
    postTransaction(body).expectStatus().isBadRequest();
  }

  @Test
  void whenPersonNotFound_thenReturn404() {
    TransactionRequest request =
        new TransactionRequest(
            LocalDate.of(2026, 6, 1),
            new BigDecimal("-12.50"),
            "RON",
            "lunch",
            null,
            null,
            999_999L,
            Source.REVOLUT);

    postTransaction(request).expectStatus().isNotFound();
  }

  @Test
  void whenTransactionNotFound_thenReturn404() {
    restTestClient.get().uri(BASE_URI + "/9999").exchange().expectStatus().isNotFound();
  }

  @Test
  void whenUncategorizedTrue_thenReturnOnlyTransactionsWithoutCategory() {
    TransactionResponse uncategorized = createTransaction(null);
    TransactionResponse categorized = createTransaction(existingCategoryId());

    try {
      PageResponse<TransactionResponse> page =
          restTestClient
              .get()
              .uri(BASE_URI + "?uncategorized=true&size=1000")
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<PageResponse<TransactionResponse>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(page).isNotNull();
      assertThat(page.content()).allMatch(t -> t.categoryId() == null);
      assertThat(page.content())
          .extracting(TransactionResponse::id)
          .contains(uncategorized.id())
          .doesNotContain(categorized.id());
    } finally {
      restTestClient
          .delete()
          .uri(BASE_URI + "/" + uncategorized.id())
          .exchange()
          .expectStatus()
          .isNoContent();
      restTestClient
          .delete()
          .uri(BASE_URI + "/" + categorized.id())
          .exchange()
          .expectStatus()
          .isNoContent();
    }
  }

  @Test
  void whenSetCategoryWithoutRule_thenCategoryIsSetAndNoRuleCreated() {
    TransactionResponse transaction = createTransaction(null);
    Long categoryId = existingCategoryId();
    int rulesBefore = countRules();
    try {
      TransactionResponse response =
          restTestClient
              .patch()
              .uri(BASE_URI + "/" + transaction.id() + "/category")
              .contentType(MediaType.APPLICATION_JSON)
              .body(new SetCategoryRequest(categoryId, null))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(TransactionResponse.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.categoryId()).isNotNull();
      assertThat(countRules()).isEqualTo(rulesBefore);
    } finally {
      restTestClient.delete().uri(BASE_URI + "/" + transaction.id()).exchange();
    }
  }

  void whenSetCategoryWithRule_thenCategoryIsSetAndRuleCreated() {}

  // PATCH: duplicate rule
  void whenSetCategoryWithDuplicatePattern_thenReturn409AndCategoryUnchanged() {}

  // PATCH: not found
  void whenSetCategoryOnUnknownTransaction_thenReturn404() {}

  void whenSetCategoryWithUnknownCategory_thenReturn404() {}

  // PATCH: validation
  void whenSetCategoryWithoutCategoryId_thenReturn400() {}

  void whenSetCategoryWithPatternShorterThan3_thenReturn400() {}

  void whenSetCategoryWithPatternPaddedToLength3_thenReturn400() {} // " ab ": bug 1

  // POST /apply-rules
  void whenApplyRules_thenMatchingUncategorizedTransactionsGetCategorized() {}

  void whenApplyRules_thenNonMatchingTransactionsStayUncategorized() {}

  void whenMoreThan20UncategorizedMatch_thenApplyRulesCategorizesAll() {} // guards the old one-page

  // bug

  private Long existingCategoryId() {
    List<CategoryResponse> categories =
        restTestClient
            .get()
            .uri("/api/categories")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<CategoryResponse>>() {})
            .returnResult()
            .getResponseBody();

    assert categories != null;
    return categories.getFirst().id();
  }

  private int countRules() {
    List<CategoryRuleResponse> rules =
        restTestClient
            .get()
            .uri("/api/category-rules")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<List<CategoryRuleResponse>>() {})
            .returnResult()
            .getResponseBody();
    assert rules != null;
    return rules.size();
  }
}
