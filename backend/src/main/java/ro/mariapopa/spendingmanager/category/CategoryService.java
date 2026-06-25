package ro.mariapopa.spendingmanager.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.mariapopa.spendingmanager.common.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return toResponse(category);
    }

    public CategoryResponse create(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.name().trim());
        category.setColor(categoryRequest.color());
        category.setMonthlyBudget(categoryRequest.monthlyBudget());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public CategoryResponse update(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setName(categoryRequest.name().trim());
        category.setColor(categoryRequest.color());
        category.setMonthlyBudget(categoryRequest.monthlyBudget());
        return toResponse(category);
    }

    public void delete(Long id) {
        if(!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getMonthlyBudget()
        );
    }
}
