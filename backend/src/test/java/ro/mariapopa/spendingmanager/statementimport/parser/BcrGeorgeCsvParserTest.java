package ro.mariapopa.spendingmanager.statementimport.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.mariapopa.spendingmanager.statementimport.ParsedTransaction;
import ro.mariapopa.spendingmanager.statementimport.StatementParseException;
import ro.mariapopa.spendingmanager.transaction.Source;

class BcrGeorgeCsvParserTest {
  private final BcrGeorgeCsvParser parser = new BcrGeorgeCsvParser();

  private static final String HEADER =
      "Issuing date of the statement,Issuing time of the statement,Starting date,End date,"
          + "Currency,BNR exchange rate,Statement issued for account,Product type,Account owner,"
          + "First opening accounting balance,Transaction completion date,"
          + "Transaction completion hour,Transaction's details,Operation's reference,"
          + "Debit (amount),Credit (amount),Total debit (amount),Total credit (amount),"
          + "Final accounting balance,Blocked amounts,Available balance,"
          + "Credit lines available limit";
  private static final String TOP_UP_ROW =
      "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
          + "Pachet pentru tineri,Popa Maria,1.89,23.06.2026,10:17,Adauga Bani Instant,"
          + "2026062315802399,0,280,0,0,0,0,0,0";
  private static final String PAYMENT_ROW =
      "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
          + "Pachet pentru tineri,Popa Maria,0,23.06.2026,10:18,Tranzactie efectuata,"
          + "2026062315803650,280,0,280,280,1.89,0,0,0";
  // K = 29.07.2026, U = 67.89, everything else blank — 22 fields, full width
  private static final String SUMMARY_ROW = ",,,,,,,,,,29.07.2026,,,,,,,,,,67.89,";

  private static final String FOUR_DIGIT_PAYMENT_ROW =
      "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
          + "Pachet pentru tineri,Popa Maria,0,23.06.2026,10:18,Tranzactie efectuata,"
          + "2026062315803650,1234.56,0,1234.56,1234.56,1.89,0,0,0";
  private static final String FOREIGN_MERCHANT_ROW =
      "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
          + "Pachet pentru tineri,Popa Maria,0,23.06.2026,10:18,"
          + "Tranzactie efectuata AMAZON.DE EUR 12.99,"
          + "2026062315803650,65,0,65,65,1.89,0,0,0";

  private ByteArrayInputStream statement(String header, String... rows) {
    List<String> lines = new ArrayList<>();
    lines.add(header);
    lines.addAll(List.of(rows));
    return new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("the parser reports BCR George as its source")
  void source_returnsBcrGeorge() {
    assertThat(parser.source()).isEqualTo(Source.BCR_GEORGE);
  }

  @Test
  @DisplayName("a debit row becomes a negative transaction")
  void debitRow_returnsNegativeAmount() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, PAYMENT_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst())
        .isEqualTo(
            new ParsedTransaction(
                LocalDate.of(2026, 6, 23),
                new BigDecimal("-280.00"),
                "RON",
                "Tranzactie efectuata"));
  }

  @Test
  @DisplayName("a credit row becomes a positive transaction")
  void creditRow_returnsPositiveAmount() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, TOP_UP_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("280.00"));
  }

  @Test
  @DisplayName("the trailing summary row with blank details does not become a transaction")
  void trailingSummaryRow_isSkipped() {
    List<ParsedTransaction> transactions =
        parser.parse(statement(HEADER, PAYMENT_ROW, SUMMARY_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().description()).isEqualTo("Tranzactie efectuata");
  }

  @Test
  @DisplayName("a quoted description containing a comma stays a single field")
  void descriptionWithEmbeddedComma_parsesAsOneField() {
    List<ParsedTransaction> transactions =
        parser.parse(
            statement(
                HEADER,
                "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
                    + "Pachet pentru tineri,Popa Maria,0,29.06.2026,11:38,\"Tranzactie efectuata, Bucuresti\","
                    + "2026062946744766,65,0,65,65,1.89,0,0,0"));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().description()).isEqualTo("Tranzactie efectuata, Bucuresti");
  }

  @Test
  @DisplayName("the currency is read from its column and uppercased")
  void currency_isReadFromColumnAndUppercased() {
    List<ParsedTransaction> transactions =
        parser.parse(
            statement(
                HEADER,
                "29.07.2026,22:00,01.06.2026,30.06.2026,ron,,RO49RNCB0000000123456789,"
                    + "Pachet pentru tineri,Popa Maria,0,29.06.2026,11:38,Tranzactie efectuata,"
                    + "2026062946744766,65,0,65,65,1.89,0,0,0"));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().currency()).isEqualTo("RON");
  }

  @Test
  @DisplayName("a statement without the Credit column is rejected")
  void missingRequiredColumn_throwsStatementParseException() {
    assertThatThrownBy(
            () -> parser.parse(statement(HEADER.replace("Credit (amount),", ""), PAYMENT_ROW)))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("Missing required columns: credit (amount)");
  }

  @Test
  @DisplayName("a blank or wrongly formatted transaction completion date is rejected")
  void blankOrMalformedDate_throwsStatementParseException() {
    assertThatThrownBy(
            () ->
                parser.parse(
                    statement(
                        HEADER,
                        "29/07/2026,22:00,01/06/2026,30/06/2026,RON,,RO49RNCB0000000123456789,"
                            + "Pachet pentru tineri,Popa Maria,0,23/06/2026,10:18,Tranzactie efectuata,"
                            + "2026062315803650,280,0,280,280,1.89,0,0,0")))
        .isInstanceOf(StatementParseException.class)
        .hasMessage(
            "row 2: Transaction completion date must be a date (dd.MM.yyyy), found \"23/06/2026\"");

    assertThatThrownBy(
            () ->
                parser.parse(
                    statement(
                        HEADER,
                        "29.07.2026,22:00,01.06.2026,30.06.2026,RON,,RO49RNCB0000000123456789,"
                            + "Pachet pentru tineri,Popa Maria,0,,10:18,Tranzactie efectuata,"
                            + "2026062315803650,280,0,280,280,1.89,0,0,0")))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 2: Transaction completion date is blank");
  }

  @Test
  @DisplayName("a four digit amount is parsed as a plain decimal with no thousands separator")
  void fourDigitAmount_parsesWithoutThousandsSeparator() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, FOUR_DIGIT_PAYMENT_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("-1234.56"));
  }

  @Test
  @DisplayName("the currency comes from the statement column, never inferred from the description")
  void currency_isAccountLevelNotTransactionLevel() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, FOREIGN_MERCHANT_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().currency()).isEqualTo("RON");
    assertThat(transactions.getFirst().description())
        .isEqualTo("Tranzactie efectuata AMAZON.DE EUR 12.99");
  }
}
