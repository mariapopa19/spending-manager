package ro.mariapopa.spendingmanager.transaction;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.category.*;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;
import ro.mariapopa.spendingmanager.person.Person;
import ro.mariapopa.spendingmanager.person.PersonRepository;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final PersonRepository personRepository;
    private final CategorizationService categorizationService;
    private final CategoryRuleService ruleService;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            PersonRepository personRepository,
            CategorizationService categorizationService,
            CategoryRuleService ruleService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.personRepository = personRepository;
        this.categorizationService = categorizationService;
        this.ruleService = ruleService;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable, boolean uncategorized) {
        if (uncategorized) {
            return transactionRepository.findByCategoryIsNull(pageable).map(this::toResponse);
        } else {
            return transactionRepository.findAll(pageable).map(this::toResponse);
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        Transaction transaction =
                transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return toResponse(transaction);
    }

    public TransactionResponse create(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();
        applyRequest(transaction, transactionRequest);

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public TransactionResponse update(Long id, TransactionRequest transactionRequest) {
        Transaction transaction =
                transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        applyRequest(transaction, transactionRequest);
        return toResponse(transaction);
    }

    public void delete(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", id);
        }
        transactionRepository.deleteById(id);
    }

    public TransactionResponse setCategory(Long id, SetCategoryRequest request) {
        Transaction transaction =
                transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
        transaction.setCategory(category);

        if (request.createRule() != null) {
            ruleService.create(new CategoryRuleRequest(request.createRule().pattern(), request.categoryId()));
        }
        return toResponse(transaction);
    }

    public int applyToUncategorized() {
        var match = categorizationService.matcher();
        int updated = 0;
        for (Transaction t : transactionRepository.findByCategoryIsNull()) {
            Optional<Category> category = match.apply(t.getDescription());
            if (category.isPresent()) {
                t.setCategory(category.get()); // dirty checking saves it, no explicit save
                updated++;
            }
        }
        return updated;
    }

    private void applyRequest(Transaction transaction, TransactionRequest transactionRequest) {
        transaction.setDate(transactionRequest.date());
        transaction.setAmount(transactionRequest.amount());
        transaction.setCurrency(transactionRequest.currency());
        transaction.setDescription(transactionRequest.description());
        transaction.setMerchant(transactionRequest.merchant());
        transaction.setSource(transactionRequest.source());

        Person person = personRepository
                .findById(transactionRequest.personId())
                .orElseThrow(() -> new ResourceNotFoundException("Person", transactionRequest.personId()));
        transaction.setPerson(person);

        if (transactionRequest.categoryId() != null) {
            Category category = categoryRepository
                    .findById(transactionRequest.categoryId())
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
                transaction.getCreatedAt());
    }
}
