package mmdev.regiveapp.category;

import mmdev.regiveapp.category.dto.CategoryRequest;
import mmdev.regiveapp.category.dto.CategoryResponse;
import mmdev.regiveapp.common.exception.DuplicateResourceException;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = "categories",allEntries = true)
    public CategoryResponse create(CategoryRequest request){
        if (categoryRepository.existsByName(request.name())){
            throw new DuplicateResourceException("Category already exists: " + request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        return toResponse(categoryRepository.save(category));
    }
    @Cacheable(cacheNames = "categories",key = "'all'")
    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "categories",allEntries = true)
    public void delete(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Category not found: id=" + id));
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category c){
        return new CategoryResponse(c.getId(),c.getName());
    }
}
