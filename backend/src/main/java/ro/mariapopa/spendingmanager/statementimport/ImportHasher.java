package ro.mariapopa.spendingmanager.statementimport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;
import ro.mariapopa.spendingmanager.transaction.Source;

public final class ImportHasher {
  private ImportHasher() {}

  public static String hash(
      LocalDate date, BigDecimal amount, String currency, String description, Source source) {
    Objects.requireNonNull(date, "date must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(currency, "currency must not be null");
    Objects.requireNonNull(description, "description must not be null");
    Objects.requireNonNull(source, "source must not be null");
    String formattedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    String canonical =
        String.join("|", date.toString(), formattedAmount, currency, description, source.name());
    byte[] digest;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      digest = messageDigest.digest(canonical.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required by the Java platform", e);
    }
    return HexFormat.of().formatHex(digest);
  }
}
