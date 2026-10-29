package ro.mariapopa.spendingmanager.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import ro.mariapopa.spendingmanager.transaction.Source;
import ro.mariapopa.spendingmanager.transaction.Transaction;

public interface StatsRepository extends Repository<Transaction, Long> {
    // Estimat vs Real: EVERY category, even with zero spending.
    // Date/currency filters go in the ON clause, not WHERE.
    // A WHERE on t.* would turn the LEFT JOIN into an inner join and drop empty categories.
    @Query("""
        select new ro.mariapopa.spendingmanager.stats.CategoryTotalResponse(
            c.id, c.name, c.color, c.monthlyBudget, coalesce(sum(-t.amount), 0bd))
        from Category c
        left join Transaction t
            on t.category = c
            and t.amount < 0
            and t.date >= :from and t.date < :to
            and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        where c.excludedFromBudget = false
        group by c.id, c.name, c.color, c.monthlyBudget
        order by c.name
        """)
    List<CategoryTotalResponse> sumByCategory(
            LocalDate from, LocalDate to, String currency, Long personId, Source source);

    // The uncategorized bucket: separate query, appended in the service
    @Query("""
        select coalesce(sum(-t.amount), 0bd)
        from Transaction t
        where t.category is null and t.amount < 0
            and t.date >= :from and t.date < :to
            and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        """)
    BigDecimal sumUncategorizedExpenses(LocalDate from, LocalDate to, String currency, Long personId, Source source);

    // Income vs expenses per month. Explicit left join so null categories are kept.
    @Query("""
        select new ro.mariapopa.spendingmanager.stats.MonthlyTotalResponse(
            concat(cast(year(t.date) as string), '-', lpad(cast(month(t.date) as string), 2, '0')),
            sum(case when t.amount < 0 and (c is null or c.excludedFromBudget = false) then -t.amount else 0bd end),
            sum(case when t.amount > 0 then t.amount else 0bd end)
        )
        from Transaction t
        left join t.category c
        where t.date >= :from and t.date < :to
            and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        group by year(t.date), month(t.date)
        order by year(t.date), month(t.date)
        """)
    List<MonthlyTotalResponse> monthly(LocalDate from, LocalDate to, String currency, Long personId, Source source);

    @Query("""
        select new ro.mariapopa.spendingmanager.stats.MerchantTotalResponse(
            coalesce(t.merchant, t.description), sum(-t.amount), count(t)
            )
        from Transaction t
        left join t.category c
        where t.amount < 0
            and (c is null or c.excludedFromBudget = false)
            and t.date >= :from and t.date < :to
            and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        group by coalesce(t.merchant, t.description)
        order by sum(-t.amount) desc
        """)
    List<MerchantTotalResponse> topMerchants(
            LocalDate from, LocalDate to, String currency, Long personId, Source source, Pageable pageable);

    @Query("""
        select coalesce(sum(t.amount), 0bd) from Transaction t
        where t.amount > 0
            and t.date >= :from and t.date < :to and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        """)
    BigDecimal sumIncome(LocalDate from, LocalDate to, String currency, Long personId, Source source);

    @Query("""
        select count(t)
        from Transaction t
        where t.category is null and t.amount < 0
            and t.date >= :from and t.date < :to
            and t.currency = :currency
            and (:personId is null or t.person.id = :personId)
            and (:source is null or t.source = :source)
        """)
    long countUncategorizedExpenses(LocalDate from, LocalDate to, String currency, Long personId, Source source);
}
