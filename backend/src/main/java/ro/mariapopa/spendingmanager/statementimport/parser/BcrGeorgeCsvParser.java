package ro.mariapopa.spendingmanager.statementimport.parser;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import ro.mariapopa.spendingmanager.statementimport.ParsedTransaction;
import ro.mariapopa.spendingmanager.statementimport.StatementParseException;
import ro.mariapopa.spendingmanager.statementimport.StatementParser;
import ro.mariapopa.spendingmanager.transaction.Source;

@Component
public class BcrGeorgeCsvParser implements StatementParser {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final Pattern CURRENCY_CODE = Pattern.compile("[A-Z]{3}");

  @Override
  public Source source() {
    return Source.BCR_GEORGE;
  }

  @Override
  public List<ParsedTransaction> parse(InputStream in) {
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
      List<CSVRecord> records = parser.getRecords();

      if (records.isEmpty()) throw new StatementParseException("No header found");

      CSVRecord header = records.getFirst();

      Map<String, Integer> columns = new HashMap<>();
      for (int c = 0; c < header.size(); c++) {
        String name = normalize(header.get(c));
        columns.put(name, c);
      }

      List<String> requiredColumns =
          List.of(
              "transaction's details",
              "transaction completion date",
              "debit (amount)",
              "credit (amount)",
              "currency");
      List<String> missing =
          requiredColumns.stream().filter(name -> !columns.containsKey(name)).toList();
      if (!missing.isEmpty()) {
        throw new StatementParseException(
            "Missing required columns: " + String.join(", ", missing));
      }

      int descriptionColumn = columnIndex(columns, "transaction's details");
      int dateColumn = columnIndex(columns, "transaction completion date");
      int debitColumn = columnIndex(columns, "debit (amount)");
      int creditColumn = columnIndex(columns, "credit (amount)");
      int currencyColumn = columnIndex(columns, "currency");

      int lastColumn =
          IntStream.of(dateColumn, descriptionColumn, debitColumn, creditColumn, currencyColumn)
              .max()
              .getAsInt();

      List<ParsedTransaction> result = new ArrayList<>();

      for (int i = 1; i < records.size(); i++) {
        CSVRecord record = records.get(i);
        long rowNumber = record.getRecordNumber();

        if (record.size() <= descriptionColumn) continue;

        String description = record.get(descriptionColumn);
        if (description.isBlank()) continue;

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
        String currency = readCurrency(record.get(currencyColumn), rowNumber);
        BigDecimal debit = parseAmountOrZero(record.get(debitColumn), rowNumber, "Debit (amount)");
        BigDecimal credit =
            parseAmountOrZero(record.get(creditColumn), rowNumber, "Credit (amount)");
        BigDecimal amount = credit.subtract(debit);

        result.add(new ParsedTransaction(date, amount, currency, description));
      }

      return result;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private String normalize(String raw) {
    return raw.replace("\uFEFF", "").replace('\u2019', '\'').trim().toLowerCase(Locale.ROOT);
  }

  private BigDecimal parseAmountOrZero(String raw, long rowNumber, String columnName) {
    String trimmed = raw.trim().replace(",", "");
    if (trimmed.isEmpty()) return BigDecimal.ZERO;
    try {
      return new BigDecimal(trimmed).setScale(2, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new StatementParseException(
          "row "
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
      throw new StatementParseException(
          "row " + rowNumber + ": Transaction completion date is blank");
    }
    try {
      return LocalDate.parse(trimmed, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new StatementParseException(
          "row "
              + rowNumber
              + ": Transaction completion date must be a date (dd.MM.yyyy), found \""
              + trimmed
              + "\"",
          e);
    }
  }

  private String readCurrency(String raw, long rowNumber) {
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new StatementParseException("row " + rowNumber + ": Currency is blank");
    }

    String upper = trimmed.toUpperCase(Locale.ROOT);
    if (!CURRENCY_CODE.matcher(upper).matches()) {
      throw new StatementParseException(
          "row " + rowNumber + ": Currency must be a 3 letter ISO code, found \"" + trimmed + "\"");
    }
    return upper;
  }

  private int columnIndex(Map<String, Integer> columns, String name) {
    Integer index = columns.get(name);
    if (index == null) throw new StatementParseException("Missing required column:" + name);
    return index;
  }
}
