package ro.mariapopa.spendingmanager.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import ro.mariapopa.spendingmanager.TestcontainersConfiguration;
import ro.mariapopa.spendingmanager.category.CategoryResponse;
import ro.mariapopa.spendingmanager.category.CategoryRuleRequest;
import ro.mariapopa.spendingmanager.category.CategoryRuleResponse;

@SpringBootTest
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public class TransactionControllerTest {
    @Autowired
    RestTestClient restTestClient;

    private static final String BASE_URI = "/api/transactions";

    @Test
    void whenUnknownSource_thenReturn400() {
        // raw String — the typed record CAN'T carry "BANANA" (the enum forbids it at compile time)
        String body = """
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
        TransactionRequest request = new TransactionRequest(
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
            PageResponse<TransactionResponse> page = restTestClient
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
            TransactionResponse response = restTestClient
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
            assertThat(response.categoryId()).isEqualTo(categoryId);
            assertThat(countRules()).isEqualTo(rulesBefore);
        } finally {
            restTestClient.delete().uri(BASE_URI + "/" + transaction.id()).exchange();
        }
    }

    @Test
    void whenSetCategoryWithRule_thenCategoryIsSetAndRuleCreated() {
        TransactionResponse transaction = createTransaction(null);
        Long categoryId = existingCategoryId();
        String pattern = "zztest-patch-rule";
        try {
            TransactionResponse response = patchCategory(
                            transaction.id(),
                            new SetCategoryRequest(categoryId, new SetCategoryRequest.RulePattern(pattern)))
                    .expectStatus()
                    .isOk()
                    .expectBody(TransactionResponse.class)
                    .returnResult()
                    .getResponseBody();
            assertThat(response).isNotNull();
            assertThat(response.categoryId()).isEqualTo(categoryId);
            assertThat(findAllRules())
                    .filteredOn(r -> r.pattern().equals(pattern))
                    .singleElement()
                    .extracting(CategoryRuleResponse::categoryId)
                    .isEqualTo(categoryId);
        } finally {
            findAllRules().stream().filter(r -> r.pattern().equals(pattern)).forEach(r -> deleteRule(r.id()));
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenSetCategoryWithDuplicatePattern_thenReturn409AndCategoryUnchanged() {
        TransactionResponse transaction = createTransaction(null);
        Long categoryId = existingCategoryId();
        CategoryRuleResponse existing = createRule("zztest-duplicate", categoryId);

        try {
            patchCategory(
                            transaction.id(),
                            new SetCategoryRequest(categoryId, new SetCategoryRequest.RulePattern("ZZTEST-DUPLICATE")))
                    .expectStatus()
                    .isEqualTo(409);
            assertThat(getTransaction(transaction.id()).categoryId()).isNull();

        } finally {
            deleteRule(existing.id());
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenSetCategoryOnUnknownTransaction_thenReturn404() {
        patchCategory(999_999L, new SetCategoryRequest(existingCategoryId(), null))
                .expectStatus()
                .isNotFound();
    }

    @Test
    void whenSetCategoryWithUnknownCategory_thenReturn404() {
        TransactionResponse transaction = createTransaction(null);
        try {
            patchCategory(transaction.id(), new SetCategoryRequest(999_999L, null))
                    .expectStatus()
                    .isNotFound();

        } finally {
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenSetCategoryWithoutCategoryId_thenReturn400() {
        TransactionResponse transaction = createTransaction(null);
        try {
            patchCategory(transaction.id(), new SetCategoryRequest(null, null))
                    .expectStatus()
                    .isBadRequest();

        } finally {
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenApplyRulesTwice_thenSecondRunUpdatesNothing() {
        CategoryRuleResponse rule = createRule("zztest-apply-twice", existingCategoryId());
        TransactionResponse transaction = createTransaction(null, "ZZTEST-APPLY-TWICE");
        try {
            assertThat(applyRules().updated()).isGreaterThanOrEqualTo(1);
            assertThat(applyRules().updated()).isZero();
        } finally {
            deleteTransaction(transaction.id());
            deleteRule(rule.id());
        }
    }

    @Test
    void whenSetCategoryWithPatternShorterThan3_thenReturn400() {
        TransactionResponse transaction = createTransaction(null);
        try {
            patchCategory(
                            transaction.id(),
                            new SetCategoryRequest(existingCategoryId(), new SetCategoryRequest.RulePattern("ab")))
                    .expectStatus()
                    .isBadRequest();
        } finally {
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenSetCategoryWithPatternPaddedToLength3_thenReturn400() {
        TransactionResponse transaction = createTransaction(null);
        // raw String on purpose: RulePattern's compact constructor would strip " ab " to "ab"
        // on the test side, before it is sent. The server's strip would then never be exercised.
        String body = """
            { "categoryId": %d, "createRule": { "pattern": " ab " } }
            """.formatted(existingCategoryId());
        try {
            patchCategory(transaction.id(), body).expectStatus().isBadRequest();
        } finally {
            deleteTransaction(transaction.id());
        }
    }

    @Test
    void whenApplyRules_thenMatchingUncategorizedTransactionsGetCategorized() {
        Long categoryId = existingCategoryId();
        CategoryRuleResponse rule = createRule("zztest-apply-match", categoryId);
        TransactionResponse transaction = createTransaction(null, "CARD ZZTEST-APPLY-MATCH 123");
        try {
            applyRules();
            assertThat(getTransaction(transaction.id()).categoryId()).isEqualTo(categoryId);
        } finally {
            deleteTransaction(transaction.id());
            deleteRule(rule.id());
        }
    }

    @Test
    void whenApplyRules_thenNonMatchingTransactionsStayUncategorized() {
        CategoryRuleResponse rule = createRule("zztest-apply-other", existingCategoryId());
        TransactionResponse transaction = createTransaction(null, "zztest nothing matches this");
        try {
            applyRules();
            assertThat(getTransaction(transaction.id()).categoryId()).isNull();
        } finally {
            deleteTransaction(transaction.id());
            deleteRule(rule.id());
        }
    }

    @Test
    void whenMoreThan20UncategorizedMatch_thenApplyRulesCategorizesAll() {
        Long categoryId = existingCategoryId();
        CategoryRuleResponse rule = createRule("zztest-apply-many", categoryId);
        List<TransactionResponse> transactions = new ArrayList<>();
        try {
            for (int i = 0; i < 25; i++) {
                transactions.add(createTransaction(null, "ZZTEST-APPLY-MANY #" + i));
            }
            applyRules();
            assertThat(transactions)
                    .allSatisfy(
                            t -> assertThat(getTransaction(t.id()).categoryId()).isEqualTo(categoryId));
        } finally {
            transactions.forEach(t -> deleteTransaction(t.id()));
            deleteRule(rule.id());
        }
    }

    private TransactionResponse createTransaction(Long categoryId) {
        return createTransaction(categoryId, "uncategorized filter test");
    }

    private TransactionResponse createTransaction(Long categoryId, String description) {
        TransactionRequest request = new TransactionRequest(
                LocalDate.of(2026, 6, 1),
                new BigDecimal("-12.50"),
                "RON",
                description,
                null,
                categoryId,
                1L,
                Source.REVOLUT);
        return postTransaction(request)
                .expectStatus()
                .isCreated()
                .expectBody(TransactionResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private TransactionResponse getTransaction(Long id) {
        return restTestClient
                .get()
                .uri(BASE_URI + "/" + id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TransactionResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private RestTestClient.ResponseSpec postTransaction(Object body) {
        return restTestClient
                .post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    private void deleteTransaction(Long id) {
        restTestClient.delete().uri(BASE_URI + "/" + id).exchange();
    }

    private CategoryRuleResponse createRule(String pattern, Long categoryId) {
        return restTestClient
                .post()
                .uri("/api/category-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CategoryRuleRequest(pattern, categoryId))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(CategoryRuleResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void deleteRule(Long id) {
        restTestClient.delete().uri("/api/category-rules/" + id).exchange();
    }

    private List<CategoryRuleResponse> findAllRules() {
        List<CategoryRuleResponse> rules = restTestClient
                .get()
                .uri("/api/category-rules")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<CategoryRuleResponse>>() {})
                .returnResult()
                .getResponseBody();
        assert rules != null;
        return rules;
    }

    private RestTestClient.ResponseSpec patchCategory(Long transactionId, Object body) {
        return restTestClient
                .patch()
                .uri(BASE_URI + "/" + transactionId + "/category")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    private Long existingCategoryId() {
        List<CategoryResponse> categories = restTestClient
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
        return findAllRules().size();
    }

    private ApplyRulesResponse applyRules() {
        return restTestClient
                .post()
                .uri(BASE_URI + "/apply-rules")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ApplyRulesResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
