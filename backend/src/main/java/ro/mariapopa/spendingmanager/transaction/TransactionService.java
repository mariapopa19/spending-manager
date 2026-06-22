package ro.mariapopa.spendingmanager.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.category.Category;
import ro.mariapopa.spendingmanager.category.CategoryRepository;
import ro.mariapopa.spendingmanager.category.CategoryResponse;
import ro.mariapopa.spendingmanager.person.Person;
import ro.mariapopa.spendingmanager.person.PersonRepository;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PersonRepository personRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, PersonRepository personRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.personRepository = personRepository;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        Category category = transaction.getCategory();
        Person person = transaction.getPerson();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDate(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getMerchant(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                category != null ? category.getColor() : null,
                person.getId(),
                person.getName(),
                transaction.getSource(),
                transaction.getImportHash(),
                transaction.getCreatedAt()
        );
    }
}
