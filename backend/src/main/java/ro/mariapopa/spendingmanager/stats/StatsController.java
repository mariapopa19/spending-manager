package ro.mariapopa.spendingmanager.stats;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.mariapopa.spendingmanager.transaction.Source;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public OverviewResponse overview(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "RON") String currency,
            @RequestParam(required = false) Long personId,
            @RequestParam(required = false) Source source) {
        return statsService.overview(month, new StatsFilter(currency, personId, source));
    }
}
