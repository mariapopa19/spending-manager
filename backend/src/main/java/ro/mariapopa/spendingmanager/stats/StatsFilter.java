package ro.mariapopa.spendingmanager.stats;

import ro.mariapopa.spendingmanager.transaction.Source;

public record StatsFilter(String currency, Long personId, Source source) {}
