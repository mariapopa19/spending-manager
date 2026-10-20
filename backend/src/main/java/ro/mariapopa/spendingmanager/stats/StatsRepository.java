package ro.mariapopa.spendingmanager.stats;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import ro.mariapopa.spendingmanager.transaction.Source;
import ro.mariapopa.spendingmanager.transaction.Transaction;

public interface StatsRepository extends Repository<Transaction, Long> {
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
}
