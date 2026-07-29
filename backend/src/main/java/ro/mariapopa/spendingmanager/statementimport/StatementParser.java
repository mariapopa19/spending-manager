package ro.mariapopa.spendingmanager.statementimport;

import java.io.InputStream;
import java.util.List;
import ro.mariapopa.spendingmanager.transaction.Source;

public interface StatementParser {
  Source source();

  List<ParsedTransaction> parse(InputStream in);
}
