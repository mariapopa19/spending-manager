package ro.mariapopa.spendingmanager.statementimport.parser;

import static org.assertj.core.api.Assertions.*;

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

class BtPayCsvParserTest {
  private final BtPayCsvParser parser = new BtPayCsvParser();

  private static final String[] PREAMBLE = {
    "Tranzactii,,,,,,",
    "Criterii de cautare,,,,,,",
    "Numar cont:,RO49BTRLRONCRT0123456789 RON,,,,,",
    "Perioada:,01.06.2026-30.06.2026,,,,,",
    ",,,,,,",
    ",,,,,,",
    "Cont,,,,,,",
    "Utilizator:,POPA MARIA,,,,,",
    "Client:,POPA MARIA,,,,,",
    "Numar cont:,RO49BTRLRONCRT0123456789 RON,,,,,",
    "Gasite:,108 Tranzactii,,,,,",
    "Rezultat cautare,,,,,,"
  };

  private static final String HEADER =
      "Data procesarii,Data tranzacției,Descriere,Referința tranzacției,Debit,Credit,Suma";
  private static final String MANGLED_HEADER =
      "Data procesarii,Data tranzac?iei,Descriere,Referin?a tranzac?ie,Debit,Credit,Suma";
  private static final String ROUND_UP_ROW =
      "30.06.2026,29.06.2026,Round Up;EPOS 30/06/2026 0640F2201,064POSI,1.16,,\"1,128.00\"";
  private static final String POS_ROW =
      "30.06.2026,29.06.2026,Plata la POS;EPOS 30/06/2026 0640F22,064POSI,138.84,,989.16";
  private static final String CREDIT_ROW =
      "30.06.2026,29.06.2026,P2P BTP - Transfer din cont 1442,043P2P,,220.00,\"1,209.16\"";

  private ByteArrayInputStream statement(String header, String... rows) {
    List<String> lines = new ArrayList<>(List.of(PREAMBLE));
    lines.add(header);
    lines.addAll(List.of(rows));
    return new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("the parser reports BT Pay as its source")
  void source_returnsBtPay() {
    assertThat(parser.source()).isEqualTo(Source.BT_PAY);
  }

  @Test
  @DisplayName("a debit row becomes a negative transaction")
  void debitRow_returnsNegativeAmount() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, POS_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst())
        .isEqualTo(
            new ParsedTransaction(
                LocalDate.of(2026, 6, 29),
                new BigDecimal("-138.84"),
                "RON",
                "Plata la POS;EPOS 30/06/2026 0640F22"));
  }

  @Test
  @DisplayName("a credit row becomes a positive transaction")
  void creditRow_returnsPositiveAmount() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, CREDIT_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("220.00"));
  }

  @Test
  @DisplayName("metadata rows above the header do not become transactions")
  void metadataRowsBeforeHeader_areSkipped() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, POS_ROW));
    assertThat(transactions).hasSize(1);
  }

  @Test
  @DisplayName("a header with mangled diacritics still resolves the date column")
  void mangledDiacriticHeader_stillResolvesColumns() {
    List<ParsedTransaction> transactions = parser.parse(statement(MANGLED_HEADER, POS_ROW));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("-138.84"));
  }

  @Test
  @DisplayName("a statement with no rows after the header contains no transactions")
  void headerOnlyStatement_returnsNoTransactions() {
    assertThat(parser.parse(statement(HEADER))).isEmpty();
  }

  @Test
  @DisplayName("a quoted description containing a newline stays a single transaction")
  void embeddedNewlineInDescription_parsesAsOneRecord() {
    List<ParsedTransaction> transactions =
        parser.parse(
            statement(
                HEADER,
                "30.06.2026,29.06.2026,\"Plata la POS \nBucurești\",064POSI,138.84,,989.16"));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().description()).isEqualTo("Plata la POS \nBucurești");
  }

  @Test
  @DisplayName("a Round Up row and its paired POS payment are both returned")
  void roundUpAndPairedPosRow_bothReturnedIndependently() {
    List<ParsedTransaction> transactions = parser.parse(statement(HEADER, ROUND_UP_ROW, POS_ROW));
    assertThat(transactions).hasSize(2);
    assertThat(transactions)
        .extracting(ParsedTransaction::amount)
        .containsExactly(new BigDecimal("-1.16"), new BigDecimal("-138.84"));
  }

  @Test
  @DisplayName("an amount with a thousands separator is parsed")
  void commaThousandsAmount_parsesCorrectly() {
    List<ParsedTransaction> transactions =
        parser.parse(
            statement(
                HEADER, "30.06.2026,29.06.2026,Salariu iulie,043SAL,,\"1,128.00 \",\"5,209.16\""));
    assertThat(transactions).hasSize(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("1128.00"));
  }

  @Test
  @DisplayName("a statement without the Credit column is rejected")
  void missingRequiredColumn_throwsStatementParseException() {
    assertThatThrownBy(
            () ->
                parser.parse(
                    statement(
                        "Data procesarii,Data tranzacției,Descriere,Referința tranzacției,Debit,Suma",
                        POS_ROW)))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("Missing required columns: credit");
  }

  @Test
  @DisplayName("a blank transaction date is rejected")
  void blankDate_throwsStatementParseException() {
    assertThatThrownBy(
            () ->
                parser.parse(statement(HEADER, "30.06.2026,,Plata la POS,064POSI,138.84,,989.16")))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 14: Data tranzacției is blank");
  }

  @Test
  @DisplayName("a transaction date in the wrong format is rejected")
  void malformedDate_throwsStatementParseException() {
    assertThatThrownBy(
            () ->
                parser.parse(
                    statement(HEADER, "30.06.2026,2026-06-29,Plata la POS,064POSI,138.84,,989.16")))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 14: Data tranzacției must be a date (dd.MM.yyyy), found \"2026-06-29\"");
  }

  @Test
  @DisplayName("a row with fewer columns than the header is rejected")
  void raggedShortRow_throwsStatementParseException() {
    assertThatThrownBy(
            () -> parser.parse(statement(HEADER, "30.06.2026,29.06.2026,Plata la POS,064POSI")))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 14: expected at least 6 columns, found 4");
  }

  @Test
  @DisplayName("a comma-only row between transactions is ignored")
  void blankRowBetweenTransactions_isSkipped() {
    List<ParsedTransaction> transactions =
        parser.parse(statement(HEADER, ROUND_UP_ROW, ",,,,,,", POS_ROW));

    assertThat(transactions).hasSize(2);
  }
}
