package ro.mariapopa.spendingmanager.statementimport.parser;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import ro.mariapopa.spendingmanager.statementimport.ParsedTransaction;
import ro.mariapopa.spendingmanager.statementimport.StatementParseException;
import ro.mariapopa.spendingmanager.statementimport.StatementParser;
import ro.mariapopa.spendingmanager.transaction.Source;

@Component
public class BtPayCsvParser implements StatementParser {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  @Override
  public Source source() {
    return Source.BT_PAY;
  }

  @Override
  public List<ParsedTransaction> parse(InputStream in) {
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
      List<CSVRecord> records = parser.getRecords();

      int headerRow = -1;
      for (int i = 0; i < records.size(); i++) {
        if (isHeader(records.get(i))) {
          headerRow = i;
          break;
        }
      }

      if (headerRow < 0) throw new StatementParseException("No header found");

      CSVRecord header = records.get(headerRow);

      int dateColumn = -1;
      Map<String, Integer> columns = new HashMap<>();
      for (int c = 0; c < header.size(); c++) {
        String name = header.get(c).trim().toLowerCase(Locale.ROOT);
        if (name.startsWith("data tranzac")) {
          dateColumn = c;
        } else if (!name.isEmpty()) {
          columns.put(name, c);
        }
      }

      if (dateColumn < 0) {
        throw new StatementParseException("Missing required column: Data tranzacției");
      }

      List<String> requiredColumns = List.of("descriere", "debit", "credit");
      List<String> missing =
          requiredColumns.stream().filter(name -> !columns.containsKey(name)).toList();
      if (!missing.isEmpty()) {
        throw new StatementParseException(
            "Missing required columns: " + String.join(", ", missing));
      }

      int descriptionColumn = columnIndex(columns, "descriere");
      int debitColumn = columnIndex(columns, "debit");
      int creditColumn = columnIndex(columns, "credit");

      int lastColumn =
          Math.max(Math.max(dateColumn, descriptionColumn), Math.max(debitColumn, creditColumn));

      List<ParsedTransaction> result = new ArrayList<>();

      for (int i = headerRow + 1; i < records.size(); i++) {
        CSVRecord record = records.get(i);
        long rowNumber = record.getRecordNumber();

        if (isBlank(record)) continue;

        if (record.size() <= lastColumn) {
          throw new StatementParseException(
              "row "
                  + rowNumber
                  + ": expected at least "
                  + (lastColumn + 1)
                  + " columns, found "
                  + record.size());
        }

        LocalDate date = parseDate(record.get(dateColumn), rowNumber);
        String description = record.get(descriptionColumn);
        BigDecimal debit = parseAmountOrZero(record.get(debitColumn), rowNumber, "Debit");
        BigDecimal credit = parseAmountOrZero(record.get(creditColumn), rowNumber, "Credit");
        BigDecimal amount = credit.subtract(debit);

        result.add(new ParsedTransaction(date, amount, "RON", description));
      }

      return result;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private boolean isHeader(CSVRecord record) {
    for (int c = 0; c < record.size(); c++) {
      if (normalize(record.get(c)).startsWith("data tranzac")) return true;
    }
    return false;
  }

  private String normalize(String raw) {
    return raw.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
  }

  private BigDecimal parseAmountOrZero(String raw, long rowNumber, String columnName) {
    String trimmed = raw.trim().replace(",", "");
    if (trimmed.isEmpty()) return BigDecimal.ZERO;
    try {
      return new BigDecimal(trimmed).setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new StatementParseException(
          "row: "
              + rowNumber
              + ": "
              + columnName
              + " must have a number, found \""
              + raw.trim()
              + "\"",
          e);
    }
  }

  private LocalDate parseDate(String raw, long rowNumber) {
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new StatementParseException("row " + rowNumber + ": Data tranzacției is blank");
    }
    try {
      return LocalDate.parse(trimmed, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new StatementParseException(
          "row "
              + rowNumber
              + ": Data tranzacției must be a date (dd.MM.yyyy), found \""
              + trimmed
              + "\"",
          e);
    }
  }

  private int columnIndex(Map<String, Integer> columns, String name) {
    Integer index = columns.get(name);
    if (index == null) throw new StatementParseException("Missing required column:" + name);
    return index;
  }

  private boolean isBlank(CSVRecord record) {
    for (int c = 0; c < record.size(); c++) {
      if (!record.get(c).isBlank()) return false;
    }
    return true;
  }
}
