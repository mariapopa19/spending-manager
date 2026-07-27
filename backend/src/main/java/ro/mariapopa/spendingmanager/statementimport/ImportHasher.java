package ro.mariapopa.spendingmanager.statementimport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import ro.mariapopa.spendingmanager.transaction.Source;

public final class ImportHasher {
  public ImportHasher() {}

  public static String hash(
      LocalDate date, BigDecimal amount, String currency, String description, Source source) {
    String formatedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    String cannonical =
        String.join("|", date.toString(), formatedAmount, currency, description, source.name());
    return null;
  }
}
