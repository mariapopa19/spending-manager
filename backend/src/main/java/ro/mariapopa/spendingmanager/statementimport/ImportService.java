package ro.mariapopa.spendingmanager.statementimport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;
import ro.mariapopa.spendingmanager.person.Person;
import ro.mariapopa.spendingmanager.person.PersonRepository;
import ro.mariapopa.spendingmanager.transaction.Source;
import ro.mariapopa.spendingmanager.transaction.Transaction;
import ro.mariapopa.spendingmanager.transaction.TransactionRepository;

@Service
@Transactional
public class ImportService {

  private final Map<Source, StatementParser> parsers;
  private final TransactionRepository transactionRepository;
  private final PersonRepository personRepository;

  // Spring injects every StatementParser bean it finds. Folding the list into a map once,
  // here, means a lookup per upload instead of a scan — and it means a duplicate Source is
  // a startup failure rather than a coin flip at runtime
  public ImportService(
      List<StatementParser> parsers,
      TransactionRepository transactionRepository,
      PersonRepository personRepository) {
    // toMap's 2-arg form throws IllegalStateException on a duplicate key. Deliberate:
    // do NOT "fix" that crash with a merge function. Two parsers claiming one Source is a
    // bug, and silently picking one hides it until the totals are wrong.
    this.parsers =
        parsers.stream() // the ingredients
            .collect(
                Collectors.toMap( // hand the recipe to the cook → get a Map
                    StatementParser::source, Function.identity()));
    this.transactionRepository = transactionRepository;
    this.personRepository = personRepository;
  }

  @Transactional(readOnly = true)
  public ImportPreviewResponse preview(Source source, InputStream in) {
    StatementParser parser = requireParser(source);

    List<ParsedTransaction> parsedTransactions = parser.parse(in);

    Set<String> seenInThisFile = new HashSet<>();
    List<ImportPreviewRow> rows = new ArrayList<>();

    for (ParsedTransaction parsedTransaction : parsedTransactions) {
      String hash =
          ImportHasher.hash(
              parsedTransaction.date(),
              parsedTransaction.amount(),
              parsedTransaction.currency(),
              parsedTransaction.description(),
              source);

      ImportStatus status =
          claimIfNew(hash, seenInThisFile) ? ImportStatus.NEW : ImportStatus.DUPLICATE;
      rows.add(
          new ImportPreviewRow(
              parsedTransaction.date(),
              parsedTransaction.amount(),
              parsedTransaction.currency(),
              parsedTransaction.description(),
              status));
    }

    return ImportPreviewResponse.of(rows);
  }

  public ImportResultResponse confirm(Source source, Long personId, List<ImportRowRequest> rows) {
    Person person =
        personRepository
            .findById(personId)
            .orElseThrow(() -> new ResourceNotFoundException("Person", personId));

    // reject rows claiming a source we can't import, so confirm can't accept
    // what preview would have refused
    requireParser(source);

    Set<String> seenInThisFile = new HashSet<>();
    List<Transaction> toSave = new ArrayList<>();

    for (ImportRowRequest row : rows) {
      String hash =
          ImportHasher.hash(row.date(), row.amount(), row.currency(), row.description(), source);

      if (claimIfNew(hash, seenInThisFile)) {
        Transaction transaction = new Transaction();
        transaction.setDate(row.date());
        transaction.setAmount(row.amount());
        transaction.setCurrency(row.currency());
        transaction.setDescription(row.description());
        transaction.setSource(source);
        transaction.setPerson(person);
        transaction.setImportHash(hash);
        toSave.add(transaction);
      }
    }

    transactionRepository.saveAll(toSave);
    return new ImportResultResponse(toSave.size(), rows.size() - toSave.size());
  }

  private StatementParser requireParser(Source source) {
    StatementParser parser = parsers.get(source);
    if (parser == null) {
      throw new StatementParseException(source + " import isn't supported yet");
    }
    return parser;
  }

  private boolean claimIfNew(String hash, Set<String> seenInThisFile) {
    return seenInThisFile.add(hash) && !transactionRepository.existsByImportHash(hash);
  }
}
