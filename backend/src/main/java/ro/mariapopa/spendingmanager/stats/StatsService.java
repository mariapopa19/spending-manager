package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {
    private final StatsRepository repository;

    public StatsService(StatsRepository repository) {
        this.repository = repository;
    }

    public List<CategoryTotalResponse> categories(YearMonth month, StatsFilter filter) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        List<CategoryTotalResponse> result = new ArrayList<>(
                repository.sumByCategory(from, to, filter.currency(), filter.personId(), filter.source()));
        BigDecimal uncategorized =
                repository.sumUncategorizedExpenses(from, to, filter.currency(), filter.personId(), filter.source());

        if (uncategorized.signum() > 0) {
            result.add(new CategoryTotalResponse(null, "Necategorizat", "#9E9E9E", null, uncategorized));
        }
        return result;
    }

    public OverviewResponse overview(YearMonth month, StatsFilter filter) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        List<CategoryTotalResponse> categories = categories(month, filter);
        BigDecimal expenses =
                categories.stream().map(CategoryTotalResponse::actual).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimated = categories.stream()
                .map(CategoryTotalResponse::estimated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal income = repository.sumIncome(from, to, filter.currency(), filter.personId(), filter.source());

        return new OverviewResponse(
                month.toString(),
                filter.currency(),
                income,
                expenses,
                estimated,
                income.subtract(expenses),
                repository.countUncategorizedExpenses(from, to, filter.currency(), filter.personId(), filter.source()));
    }

    public List<MonthlyTotalResponse> monthly(YearMonth from, YearMonth to, StatsFilter filter) {
        LocalDate fromDate = from.atDay(1);
        LocalDate toExclusive = to.plusMonths(1).atDay(1); // includes the whole `to` month

        return repository.monthly(fromDate, toExclusive, filter.currency(), filter.personId(), filter.source());
    }

    public List<MerchantTotalResponse> topMerchants(YearMonth month, int limit, StatsFilter filter) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.plusMonths(1).atDay(1);

        return repository.topMerchants(
                from, to, filter.currency(), filter.personId(), filter.source(), PageRequest.of(0, limit));
    }
}
