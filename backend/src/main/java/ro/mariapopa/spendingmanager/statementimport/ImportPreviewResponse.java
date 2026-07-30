package ro.mariapopa.spendingmanager.statementimport;

import java.util.List;

public record ImportPreviewResponse(
    int total, int newCount, int duplicateCount, List<ImportPreviewRow> rows) {
  public static ImportPreviewResponse of(List<ImportPreviewRow> rows) {
    long newCount = rows.stream().filter(row -> row.status() == ImportStatus.NEW).count();
    return new ImportPreviewResponse(
        rows.size(), (int) newCount, rows.size() - (int) newCount, List.copyOf(rows));
  }
}
