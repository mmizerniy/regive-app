package mmdev.regiveapp.category;


import jakarta.validation.Valid;
import mmdev.regiveapp.category.dto.CategoryRequest;
import mmdev.regiveapp.category.dto.CategoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request){
        CategoryResponse created = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/categories/"+created.id())).body(created);
    }

    @GetMapping
    public List<CategoryResponse> findAll(){
        return categoryService.findAll();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
