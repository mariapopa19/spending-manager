package ro.mariapopa.spendingmanager.statementimport;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.mariapopa.spendingmanager.transaction.Source;

public class ImportHasherTest {
  private static final LocalDate DATE = LocalDate.of(2026, 6, 2);
  private static final BigDecimal AMOUNT = new BigDecimal("-220.00");
  private static final String CURRENCY = "RON";
  private static final String DESCRIPTION = "Card payment to Lidl";
  ;

  @Test
  @DisplayName("the same input always produce the same hash")
  void sameInputs_produceSameHash() {
    String revolut1 = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    String revolut2 = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    assertThat(revolut1).isEqualTo(revolut2);
  }

  @Test
  @DisplayName("123.4 and 123.40 are the same amount, so the same hash")
  void amountScaleDoesNotChangeHash() {
    // TODO(you)
  }

  @Test
  @DisplayName("the same purchase from a different bank is a different transaction")
  void differentSource_producesDifferentHash() {
    // TODO(you)
  }

  @Test
  @DisplayName("the hash is 64 lowercase hex characters")
  void hashIsSixtyFourHexChars() {
    // TODO(you)
  }

  @Test
  @DisplayName("the canonical format is frozen (golden value)")
  void matchesGoldenHash() {
    // TODO(you)
  }

  @Test
  @DisplayName("Romanian diacritics hash as UTF-8, not the platform default")
  void diacriticsAreHashedAsUtf8() {
    // TODO(you)
  }
}
