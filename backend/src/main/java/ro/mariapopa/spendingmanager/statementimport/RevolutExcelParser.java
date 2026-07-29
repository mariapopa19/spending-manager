package ro.mariapopa.spendingmanager.statementimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ro.mariapopa.spendingmanager.transaction.Source;

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
      Map<String, Integer> columns = new HashMap<>();
      Row header = sheet.getRow(0);
      for (int c = 0; c < header.getLastCellNum(); c++) {
        Cell cell = header.getCell(c);
        if (cell != null) {
          columns.put(cell.getStringCellValue().trim().toLowerCase(), c);
        }
      }

      // data rows
      List<ParsedTransaction> result = new ArrayList<>();
      for (int r = 1; r <= sheet.getLastRowNum(); r++) {
        Row row = sheet.getRow(r);
        if (row == null) continue;

        // filter rows in a non-completed state
        String state = row.getCell(columns.get("state")).getStringCellValue();
        if (!"COMPLETED".equals(state)) continue;

        LocalDate date =
            row.getCell(columns.get("completed date")).getLocalDateTimeCellValue().toLocalDate();
        String description = row.getCell(columns.get("description")).getStringCellValue();
        String currency = row.getCell(columns.get("currency")).getStringCellValue().toUpperCase();
        BigDecimal amount =
            BigDecimal.valueOf(row.getCell(columns.get("amount")).getNumericCellValue())
                .setScale(2, RoundingMode.HALF_UP);

        result.add(new ParsedTransaction(date, amount, currency, description));
      }

      return result;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
