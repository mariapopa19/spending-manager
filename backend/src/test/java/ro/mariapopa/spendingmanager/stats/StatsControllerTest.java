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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import ro.mariapopa.spendingmanager.category.CategoryResponse;
import ro.mariapopa.spendingmanager.transaction.Source;
import ro.mariapopa.spendingmanager.transaction.TransactionRequest;
import ro.mariapopa.spendingmanager.transaction.TransactionResponse;

@SpringBootTest
@AutoConfigureRestTestClient
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
                "/merchants?month=2001-01&limit=x" // non-numeric limit
            })
    void whenMonthIsMissingOrMalformed_thenReturn400(String path) {
        restTestClient.get().uri(BASE_URI + path).exchange().expectStatus().isBadRequest();
    }

    @Test
    void whenCategoryHasNoTransactions_thenItStillAppearsWithZeroActual() {}

    @Test
    void whenExpenseIsInEconomii_thenItIsExcludedFromCategoriesAndMonthly() {}

    @Test
    void whenExpenseHasNoCategory_thenItAppearsAsNecategorizat() {}

    @Test
    void whenTransactionIsOnFirstOfNextMonth_thenItIsNotCounted() {
        Long rate = categoryId("Rate");
        create(LocalDate.of(2001, 4, 30), "-10.00", rate);
        create(LocalDate.of(2001, 5, 1), "-99.00", rate);

        assertThat(actualFor(getCategories("month=2001-04"), "Rate")).isEqualByComparingTo("10.00");
    }

    @Test
    void whenTransactionIsInEur_thenItIsNotCountedForRon() {}

    @Test
    void whenFilteringByPersonOrSource_thenOnlyMatchingTransactionsCount() {}

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
    void whenOverview_thenRemainingIsIncomeMinusExpensesWithoutEconomii() {}

    @Test
    void whenTopMerchants_thenSortedByTotalDescAndLimited() {}

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
