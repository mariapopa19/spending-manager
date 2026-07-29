package ro.mariapopa.spendingmanager.statementimport;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.mariapopa.spendingmanager.transaction.Source;

class RevolutExcelParserTest {
  private final RevolutExcelParser parser = new RevolutExcelParser();

  private static final LocalDate DATE = LocalDate.of(2026, 6, 2);
  private static final BigDecimal AMOUNT = new BigDecimal("-220.00");
  private static final String CURRENCY = "RON";
  private static final String DESCRIPTION = "Card payment to Lidl";

  @Test
  @DisplayName("the parser reports Revolut as its source")
  void source_returnsRevolut() {
    assertThat(parser.source()).isEqualTo(Source.REVOLUT);
  }

  @Test
  @DisplayName("a completed Revolut row becomes a parsed transaction")
  void completedRow_returnsParsedTransaction() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);

              row.createCell(0).setCellValue("COMPLETED");
              CellStyle dateStyle = workbook.createCellStyle();
              dateStyle.setDataFormat(
                  workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

              row.createCell(1).setCellValue(Date.valueOf(DATE));
              row.getCell(1).setCellStyle(dateStyle);

              row.createCell(2).setCellValue(DESCRIPTION);
              row.createCell(3).setCellValue(CURRENCY);
              row.createCell(4).setCellValue(AMOUNT.doubleValue());
            });

    List<ParsedTransaction> transactions = parser.parse(input);

    assertThat(transactions.size()).isEqualTo(1);
    assertThat(transactions.getFirst())
        .isEqualTo(new ParsedTransaction(DATE, AMOUNT, CURRENCY, DESCRIPTION));
  }

  @Test
  @DisplayName("a pending Revolut row is ignored")
  void pendingRow_returnsNoTransactions() throws IOException {

    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);
              row.createCell(0).setCellValue("PENDING");
            });

    List<ParsedTransaction> transactions = parser.parse(input);

    assertThat(transactions.size()).isZero();
  }

  @Test
  @DisplayName("a workbook without the Amount column is rejected")
  void missingAmountColumn_throwsStatementParseException() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet();

      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("State");
      header.createCell(1).setCellValue("Completed Date");
      header.createCell(2).setCellValue("Description");
      header.createCell(3).setCellValue("Currency");

      workbook.write(output);

      assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(output.toByteArray())))
          .isInstanceOf(StatementParseException.class)
          .hasMessage("Missing required columns: amount");
    }
  }

  @Test
  @DisplayName("a completed row with a text date is rejected")
  void completedRowWithTextDate_throwsStatementParseException() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);
              row.createCell(0).setCellValue("COMPLETED");
              row.createCell(1).setCellValue("2026-06-02");
            });

    assertThatThrownBy(() -> parser.parse(input))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 2: Completed Date must be a date");
  }

  @Test
  @DisplayName("headers are matched case-insensitively and after trimming whitespace")
  void headersWithDifferentCaseAndWhitespace_areAccepted() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet();

      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue(" state ");
      header.createCell(1).setCellValue("COMPLETED DATE");
      header.createCell(2).setCellValue(" Description ");
      header.createCell(3).setCellValue("Currency");
      header.createCell(4).setCellValue(" Amount");

      Row row = sheet.createRow(1);
      row.createCell(0).setCellValue("COMPLETED");
      CellStyle dateStyle = workbook.createCellStyle();
      dateStyle.setDataFormat(
          workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

      row.createCell(1).setCellValue(Date.valueOf(DATE));
      row.getCell(1).setCellStyle(dateStyle);

      row.createCell(2).setCellValue(DESCRIPTION);
      row.createCell(3).setCellValue(CURRENCY);
      row.createCell(4).setCellValue(AMOUNT.doubleValue());

      workbook.write(output);

      List<ParsedTransaction> transactions =
          parser.parse(new ByteArrayInputStream(output.toByteArray()));

      assertThat(transactions.size()).isEqualTo(1);
    }
  }

  @Test
  @DisplayName("a workbook with no header row is rejected")
  void emptySheet_throwsStatementParseException() throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      workbook.createSheet();
      workbook.write(output);
      assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(output.toByteArray())))
          .isInstanceOf(StatementParseException.class)
          .hasMessage("Sheet is empty: no header row found");
    }
  }

  @Test
  @DisplayName("a header-only workbook contains no transactions")
  void headerOnlyWorkbook_returnsNoTransactions() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
            });
    List<ParsedTransaction> transactions = parser.parse(input);
    assertThat(transactions.size()).isZero();
  }

  @Test
  @DisplayName("a completed row with a blank amount is rejected")
  void completedRowWithBlankAmount_throwsStatementParseException() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);

              row.createCell(0).setCellValue("COMPLETED");
              CellStyle dateStyle = workbook.createCellStyle();
              dateStyle.setDataFormat(
                  workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

              row.createCell(1).setCellValue(Date.valueOf(DATE));
              row.getCell(1).setCellStyle(dateStyle);

              row.createCell(2).setCellValue(DESCRIPTION);
              row.createCell(3).setCellValue(CURRENCY);
            });

    assertThatThrownBy(() -> parser.parse(input))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 2: Amount is blank");
  }

  @Test
  @DisplayName("currency is normalized to uppercase")
  void completedRowWithLowercaseCurrency_returnsUppercaseCurrency() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);

              row.createCell(0).setCellValue("COMPLETED");
              CellStyle dateStyle = workbook.createCellStyle();
              dateStyle.setDataFormat(
                  workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

              row.createCell(1).setCellValue(Date.valueOf(DATE));
              row.getCell(1).setCellStyle(dateStyle);

              row.createCell(2).setCellValue(DESCRIPTION);
              row.createCell(3).setCellValue(" ron ");
              row.createCell(4).setCellValue(AMOUNT.doubleValue());
            });

    List<ParsedTransaction> transactions = parser.parse(input);

    assertThat(transactions.size()).isEqualTo(1);
    assertThat(transactions.getFirst().currency()).isEqualTo("RON");
  }

  @Test
  @DisplayName("a text amount is parsed and normalized to two decimal places")
  void completedRowWithTextAmount_parsesAndNormalizesScale() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);

              row.createCell(0).setCellValue("COMPLETED");
              CellStyle dateStyle = workbook.createCellStyle();
              dateStyle.setDataFormat(
                  workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

              row.createCell(1).setCellValue(Date.valueOf(DATE));
              row.getCell(1).setCellStyle(dateStyle);

              row.createCell(2).setCellValue(DESCRIPTION);
              row.createCell(3).setCellValue(CURRENCY);
              row.createCell(4).setCellValue(new BigDecimal("-123.4").doubleValue());
            });

    List<ParsedTransaction> transactions = parser.parse(input);

    assertThat(transactions.size()).isEqualTo(1);
    assertThat(transactions.getFirst().amount()).isEqualTo(new BigDecimal("-123.40"));
  }

  @Test
  @DisplayName("a non-numeric amount is rejected")
  void completedRowWithNonNumericAmount_throwsStatementParseException() throws IOException {
    ByteArrayInputStream input =
        workbookInput(
            (workbook, sheet) -> {
              createRequiredHeader(sheet);
              Row row = sheet.createRow(1);

              row.createCell(0).setCellValue("COMPLETED");
              CellStyle dateStyle = workbook.createCellStyle();
              dateStyle.setDataFormat(
                  workbook.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));

              row.createCell(1).setCellValue(Date.valueOf(DATE));
              row.getCell(1).setCellStyle(dateStyle);

              row.createCell(2).setCellValue(DESCRIPTION);
              row.createCell(3).setCellValue(CURRENCY);
              row.createCell(4).setCellValue("test");
            });

    assertThatThrownBy(() -> parser.parse(input))
        .isInstanceOf(StatementParseException.class)
        .hasMessage("row 2: Amount must be a number");
  }

  private ByteArrayInputStream workbookInput(BiConsumer<XSSFWorkbook, Sheet> setup)
      throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet();

      setup.accept(workbook, sheet);

      workbook.write(output);
      return new ByteArrayInputStream(output.toByteArray());
    }
  }

  private void createRequiredHeader(Sheet sheet) {
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("State");
    header.createCell(1).setCellValue("Completed Date");
    header.createCell(2).setCellValue("Description");
    header.createCell(3).setCellValue("Currency");
    header.createCell(4).setCellValue("Amount");
  }
}
