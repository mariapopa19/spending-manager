package ro.mariapopa.spendingmanager.statementimport.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ro.mariapopa.spendingmanager.statementimport.ParsedTransaction;
import ro.mariapopa.spendingmanager.statementimport.StatementParseException;
import ro.mariapopa.spendingmanager.statementimport.StatementParser;
import ro.mariapopa.spendingmanager.transaction.Source;

@Component
public class RevolutExcelParser implements StatementParser {
  @Override
  public Source source() {
    return Source.REVOLUT;
  }

  @Override
  public List<ParsedTransaction> parse(InputStream in) {
    try (Workbook workbook = new XSSFWorkbook(in)) {
      Sheet sheet = workbook.getSheetAt(0); // first tab of the excel

      // header row -> column index
      Row header = sheet.getRow(0);
      if (header == null) {
        throw new StatementParseException("Sheet is empty: no header row found");
      }

      Map<String, Integer> columns = new HashMap<>();
      for (int c = 0; c < header.getLastCellNum(); c++) {
        Cell cell = header.getCell(c);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
          continue;
        }

        if (cell.getCellType() != CellType.STRING) {
          throw new StatementParseException("Header column " + (c + 1) + " must be text");
        }

        String name = cell.getStringCellValue().trim().toLowerCase(Locale.ROOT);
        if (!name.isEmpty()) {
          columns.put(name, c);
        }
      }

      List<String> requiredColumns =
          List.of("state", "completed date", "description", "currency", "amount");

      List<String> missingColumns =
          requiredColumns.stream().filter(name -> !columns.containsKey(name)).toList();

      if (!missingColumns.isEmpty()) {
        throw new StatementParseException(
            "Missing required columns: " + String.join(", ", missingColumns));
      }

      int stateColumn = columnIndex(columns, "state");
      int dateColumn = columnIndex(columns, "completed date");
      int descriptionColumn = columnIndex(columns, "description");
      int currencyColumn = columnIndex(columns, "currency");
      int amountColumn = columnIndex(columns, "amount");

      // data rows
      List<ParsedTransaction> result = new ArrayList<>();
      for (int r = 1; r <= sheet.getLastRowNum(); r++) {
        Row row = sheet.getRow(r);
        if (row == null || isBlank(row)) continue;

        // filter rows in a non-completed state
        String state = readString(row, stateColumn, "State");
        if (!"COMPLETED".equals(state)) continue;

        LocalDate date = readDate(row, dateColumn);
        String description = readString(row, descriptionColumn, "Description");
        String currency = readString(row, currencyColumn, "Currency").toUpperCase(Locale.ROOT);
        BigDecimal amount = readAmount(row, amountColumn);

        result.add(new ParsedTransaction(date, amount, currency, description));
      }

      return result;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private boolean isBlank(Row row) {
    if (row.getFirstCellNum() < 0) return true;
    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
      Cell cell = row.getCell(c);
      if (!(cell == null
          || cell.getCellType() == CellType.BLANK
          || cell.getStringCellValue().isBlank())) {
        return false;
      }
    }
    return true;
  }

  private int columnIndex(Map<String, Integer> columns, String name) {
    Integer index = columns.get(name);
    if (index == null) throw new StatementParseException("Missing required column:" + name);
    return index;
  }

  private String readString(Row row, int col, String columnName) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK)
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + columnName + " is blank");

    try {
      String value = cell.getStringCellValue().trim();
      if (value.isEmpty()) {
        throw new StatementParseException(
            "row " + (row.getRowNum() + 1) + ": " + columnName + " is blank");
      }
      return value;
    } catch (IllegalStateException e) {
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + columnName + " must be text", e);
    }
  }

  private LocalDate readDate(Row row, int col) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK)
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + "Completed Date" + " is blank");

    try {
      if (cell.getCellType() != CellType.NUMERIC || !DateUtil.isCellDateFormatted(cell)) {
        throw new StatementParseException(
            "row " + (row.getRowNum() + 1) + ": " + "Completed Date" + " must be a date");
      }
      return cell.getLocalDateTimeCellValue().toLocalDate();
    } catch (IllegalStateException e) {
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + "Completed Date" + " must be a date", e);
    }
  }

  private BigDecimal readAmount(Row row, int col) {
    Cell cell = row.getCell(col);
    if (cell == null || cell.getCellType() == CellType.BLANK)
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + "Amount" + " is blank");

    try {
      BigDecimal amount =
          switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> new BigDecimal(cell.getStringCellValue().trim());
            default ->
                throw new StatementParseException(
                    "row " + (row.getRowNum() + 1) + ": " + "Amount" + " must be a number");
          };
      return amount.setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException | IllegalStateException e) {
      throw new StatementParseException(
          "row " + (row.getRowNum() + 1) + ": " + "Amount" + " must be a number", e);
    }
  }
}
