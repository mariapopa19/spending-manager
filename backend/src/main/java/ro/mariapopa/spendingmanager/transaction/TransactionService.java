package ro.mariapopa.spendingmanager.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.category.Category;
import ro.mariapopa.spendingmanager.category.CategoryRepository;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;
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

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return toResponse(transaction);
    }

    public TransactionResponse create(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();
        applyRequest(transaction, transactionRequest);

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public TransactionResponse update(Long id, TransactionRequest transactionRequest) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        applyRequest(transaction, transactionRequest);
        return toResponse(transaction);
    }

    public void delete(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", id);
        }
        transactionRepository.deleteById(id);
    }

    private void applyRequest(Transaction transaction, TransactionRequest transactionRequest) {
        transaction.setDate(transactionRequest.date());
        transaction.setAmount(transactionRequest.amount());
        transaction.setCurrency(transactionRequest.currency());
        transaction.setDescription(transactionRequest.description());
        transaction.setMerchant(transactionRequest.merchant());
        transaction.setSource(transactionRequest.source());

        Person person = personRepository.findById(transactionRequest.personId())
                .orElseThrow(() -> new ResourceNotFoundException("Person", transactionRequest.personId()));
        transaction.setPerson(person);

        if (transactionRequest.categoryId() != null) {
            Category category = categoryRepository.findById(transactionRequest.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", transactionRequest.categoryId()));
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }

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
