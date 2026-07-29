package ro.mariapopa.spendingmanager.statementimport;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.mariapopa.spendingmanager.transaction.Source;

class ImportHasherTest {
  private static final LocalDate DATE = LocalDate.of(2026, 6, 2);
  private static final BigDecimal AMOUNT = new BigDecimal("-220.00");
  private static final String CURRENCY = "RON";
  private static final String DESCRIPTION = "Card payment to Lidl";

  @Test
  @DisplayName("the same input always produce the same hash")
  void sameInputs_produceSameHash() {
    String revolutHash1 = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    String revolutHash2 = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    assertThat(revolutHash1).isEqualTo(revolutHash2);
  }

  @Test
  @DisplayName("123.4 and 123.40 are the same amount, so the same hash")
  void amountScaleDoesNotChangeHash() {
    String revolutHash =
        ImportHasher.hash(DATE, new BigDecimal("-123.4"), CURRENCY, DESCRIPTION, Source.REVOLUT);
    String btPayHash =
        ImportHasher.hash(DATE, new BigDecimal("-123.40"), CURRENCY, DESCRIPTION, Source.REVOLUT);
    assertThat(revolutHash).isEqualTo(btPayHash);
  }

  @Test
  @DisplayName("the same purchase from a different bank is a different transaction")
  void differentSource_producesDifferentHash() {
    String revolutHash = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    String btPayHash = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.BT_PAY);
    assertThat(revolutHash).isNotEqualTo(btPayHash);
  }

  @Test
  @DisplayName("the hash is 64 lowercase hex characters")
  void hashIsSixtyFourHexChars() {
    String revolutHash = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    assertThat(revolutHash).matches("[0-9a-f]{64}");
  }

  @Test
  @DisplayName("the canonical format is frozen (golden value)")
  void matchesGoldenHash() {
    String revolutHash = ImportHasher.hash(DATE, AMOUNT, CURRENCY, DESCRIPTION, Source.REVOLUT);
    assertThat(revolutHash)
        .isEqualTo("595e4b5aca66a855ef832e4cf4d16a736d78ce3608bf56bd777ffc49a8270ee8");
  }

  @Test
  @DisplayName("Romanian diacritics hash as UTF-8, not the platform default")
  void diacriticsAreHashedAsUtf8() {
    String revolutHash =
        ImportHasher.hash(DATE, AMOUNT, CURRENCY, "Mâncare la Ieșit în oraș", Source.REVOLUT);
    assertThat(revolutHash)
        .isEqualTo("eccc0c305e5dbb3b14123d094fc41ad40a85d6cc86cabd6b314a9a49746aa8bf");
  }

  @Test
  @DisplayName("a null description is rejected, not hashed as \"null\"")
  void nullDescription_throwsNullPointerException() {
    assertThatThrownBy(() -> ImportHasher.hash(DATE, AMOUNT, CURRENCY, null, Source.REVOLUT))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("description");
  }
}
