package ro.mariapopa.spendingmanager.stats;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    //TODO
    public MonthlyTotalResponse monthly(YearMonth month, StatsFilter filter) {return null;}

    //TODO
    public MerchantTotalResponse topMerchants(YearMonth month, StatsFilter filter) {return null;}

}
