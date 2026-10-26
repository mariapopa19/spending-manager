package ro.mariapopa.spendingmanager.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import ro.mariapopa.spendingmanager.TestcontainersConfiguration;
import ro.mariapopa.spendingmanager.category.CategoryResponse;
import ro.mariapopa.spendingmanager.common.ApiError;
import ro.mariapopa.spendingmanager.transaction.Source;
import ro.mariapopa.spendingmanager.transaction.TransactionRequest;
import ro.mariapopa.spendingmanager.transaction.TransactionResponse;

@SpringBootTest
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public class StatsControllerTest {
    @Autowired
    RestTestClient restTestClient;

    private static final String BASE_URI = "/api/stats";

    private static final long CRISTI = 1L;
    private static final long MARIA = 2L;

    private final List<Long> createdIds = new ArrayList<>();

    private void create(LocalDate date, String amount, Long categoryId) {
        create(date, amount, "RON", null, categoryId, CRISTI, Source.MANUAL);
    }

    private void create(
            LocalDate date,
            String amount,
            String currency,
            String merchant,
            Long categoryId,
            long personId,
            Source source) {
        TransactionRequest request = new TransactionRequest(
                date, new BigDecimal(amount), currency, "zztest stats", merchant, categoryId, personId, source);
        TransactionResponse response = restTestClient
                .post()
                .uri("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(TransactionResponse.class)
                .returnResult()
                .getResponseBody();
        assert response != null;
        createdIds.add(response.id());
    }

    @AfterEach
    void cleanUp() {
        createdIds.forEach(
                id -> restTestClient.delete().uri("/api/transactions/" + id).exchange());
        createdIds.clear();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/overview?month=2001-01",
                "/categories?month=2001-01",
                "/monthly?from=2001-01&to=2001-12",
                "/merchants?month=2001-01"
            })
    void whenNoPersonOrSourceFilter_thenEveryEndpointReturns200(String path) {
        restTestClient.get().uri(BASE_URI + path).exchange().expectStatus().isOk();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/categories", // missing
                "/categories?month=2001-13", // invalid month number
                "/categories?month=abc", // not a date at all
                "/monthly?from=2001-01", // missing `to`
                "/merchants?month=2001-01&limit=x", // non-numeric limit
                "/merchants?month=2001-01&limit=0"
            })
    void whenMonthIsMissingOrMalformed_thenReturn400(String path) {
        restTestClient.get().uri(BASE_URI + path).exchange().expectStatus().isBadRequest();
    }

    @Test
    void whenLimitIsNotPositive_thenReturn400WithLimitFieldError() {
        ApiError error = restTestClient
                .get()
                .uri(BASE_URI + "/merchants?month=2001-01&limit=0")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ApiError.class)
                .returnResult()
                .getResponseBody();

        assertThat(error).isNotNull();
        assertThat(error.message()).isEqualTo("Validation failed");
        assertThat(error.fieldErrors()).containsKey("limit");
    }

    @Test
    void whenCategoryHasNoTransactions_thenItStillAppearsWithZeroActual() {
        List<CategoryTotalResponse> result = getCategories("month=2001-02");

        assertThat(actualFor(result, "Medicale")).isEqualByComparingTo("0");
        assertThat(result).extracting(CategoryTotalResponse::name).doesNotContain("Economii", "Necategorizat");
    }

    @Test
    void whenExpenseIsInEconomii_thenItIsExcludedFromCategoriesAndMonthly() {
        create(LocalDate.of(2001, 8, 15), "-300.00", categoryId("Economii"));
        create(LocalDate.of(2001, 8, 30), "-10.00", categoryId("Rate"));

        List<CategoryTotalResponse> result = getCategories("month=2001-08");
        assertThat(result).extracting(CategoryTotalResponse::name).doesNotContain("Economii");
        assertThat(getMonthly("from=2001-08&to=2001-08")).extracting(MonthlyTotalResponse::expenses).contains(new  BigDecimal("10.00"));
        assertThat(actualFor(result, "Rate")).isEqualByComparingTo("10.00");
    }

    @Test
    void whenExpenseHasNoCategory_thenItAppearsAsNecategorizat() {
        /* - Create one expense with categoryId = null, then check actualFor(…, "Necategorizat").
        - Optionally also check that the row's categoryId() is null. The frontend will depend on that.*/
        create(LocalDate.of(2001, 8, 15), "-300.00", null);
        List<CategoryTotalResponse> result = getCategories("month=2001-08");

        assertThat(actualFor(result, "Necategorizat")).isEqualByComparingTo("300.00");
        assertThat(result).extracting(CategoryTotalResponse::categoryId).contains((Long) null);
    }

    @Test
    void whenTransactionIsOnFirstOfNextMonth_thenItIsNotCounted() {
        Long rate = categoryId("Rate");
        create(LocalDate.of(2001, 4, 30), "-10.00", rate);
        create(LocalDate.of(2001, 5, 1), "-99.00", rate);

        assertThat(actualFor(getCategories("month=2001-04"), "Rate")).isEqualByComparingTo("10.00");
    }

    @Test
    void whenTransactionIsInEur_thenItIsNotCountedForRon() {
        /*- Use the long create(...) overload with "EUR".
        - Check both directions: the RON query ignores the EUR transaction, and month=…&currency=EUR counts only it. With both checks, the test proves the filter
          works, not just that the EUR row is missing.*/
        create(LocalDate.of(2001, 8, 20), "-300.00", "EUR", "Mega Image", categoryId("Lifestyle"), MARIA, Source.BT_PAY);
        create(LocalDate.of(2001, 8, 30), "-10.00", categoryId("Lifestyle"));

        assertThat(actualFor(getCategories("month=2001-08"), "Lifestyle")).isEqualByComparingTo("10.00");
    }

    @Test
    void whenFilteringByPersonOrSource_thenOnlyMatchingTransactionsCount() {
        /* - Create three Rate expenses with different amounts (10, 20, 40):
          - CRISTI/MANUAL
          - MARIA/MANUAL
          - CRISTI/REVOLUT
        - personId=2 should give 20, source=REVOLUT should give 40, and both filters together should give 0.
        - Use different amounts so each sum shows which rows were counted.
        - If this test gets too long, split it into one test per filter.*/
    }

    @Test
    void whenMonthlyRangeSpansTwoMonths_thenBothAreReturnedInOrderIncludingTheToMonth() {
        Long rate = categoryId("Rate");
        create(LocalDate.of(2001, 8, 15), "-10.00", rate);
        create(LocalDate.of(2001, 9, 30), "-20.00", rate);
        create(LocalDate.of(2001, 9, 15), "500.00", null);
        create(LocalDate.of(2001, 10, 1), "-99.00", rate); // outside the range

        List<MonthlyTotalResponse> result = getMonthly("from=2001-08&to=2001-09");

        assertThat(result).extracting(MonthlyTotalResponse::month).containsExactly("2001-08", "2001-09");
        assertThat(result.get(1).expenses()).isEqualByComparingTo("20.00");
        assertThat(result.get(1).income()).isEqualByComparingTo("500.00");
    }

    @Test
    void whenOverview_thenRemainingIsIncomeMinusExpensesWithoutEconomii() {
        /* - In one month, create:
          - income of +1000, uncategorized
          - Rate -100
          - Economii -300
          - an uncategorized expense of -50
        - Expect:
          - income = 1000
          - expenses = 150 (Economii excluded, the uncategorized expense included)
          - remaining = 850
          - uncategorizedCount = 1. The +1000 income has no category but must not be counted, because only expenses are.
          - estimated = sumOfBudgets(). That's the job of the helper you already wrote.*/
    }

    @Test
    void whenTopMerchants_thenSortedByTotalDescAndLimited() {
        /*- Create:
          - Lidl -10 and -20 (total 30, count 2)
          - Kaufland -50
          - Mega -5
        - Call with limit=2 and expect [Kaufland, Lidl] in that order, with Lidl's count = 2.
        - Two things to watch:
          - Use different totals. If two merchants tie, the order isn't guaranteed and the test will fail at random.
          - Give every transaction a merchant. A null merchant falls back to the description, and all your test rows share the description "zztest stats", so
            they'd merge into one group.*/
    }

    private OverviewResponse getOverview(String query) {
        return restTestClient
                .get()
                .uri(BASE_URI + "/overview?" + query)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<OverviewResponse>() {})
                .returnResult()
                .getResponseBody();
    }

    private List<CategoryTotalResponse> getCategories(String query) {
        return restTestClient
                .get()
                .uri(BASE_URI + "/categories?" + query)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<CategoryTotalResponse>>() {})
                .returnResult()
                .getResponseBody();
    }

    private List<MonthlyTotalResponse> getMonthly(String query) {
        return restTestClient
                .get()
                .uri(BASE_URI + "/monthly?" + query)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<MonthlyTotalResponse>>() {})
                .returnResult()
                .getResponseBody();
    }

    private List<MerchantTotalResponse> getTopMerchants(String query) {
        return restTestClient
                .get()
                .uri(BASE_URI + "/merchants?" + query)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<List<MerchantTotalResponse>>() {})
                .returnResult()
                .getResponseBody();
    }

    private BigDecimal actualFor(List<CategoryTotalResponse> categories, String name) {
        return categories.stream()
                .filter(category -> name.equals(category.name()))
                .findFirst()
                .orElseThrow()
                .actual();
    }

    private List<CategoryResponse> allCategories() {
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
        return categories;
    }

    private Long categoryId(String categoryName) {
        return allCategories().stream()
                .filter(category -> categoryName.equals(category.name()))
                .findFirst()
                .orElseThrow()
                .id();
    }

    private BigDecimal sumOfBudgets() {
        return allCategories().stream()
                .filter(category -> !category.excludedFromBudget())
                .map(CategoryResponse::monthlyBudget)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
