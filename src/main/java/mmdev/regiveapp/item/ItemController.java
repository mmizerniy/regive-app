package mmdev.regiveapp.item;

import jakarta.validation.Valid;
import mmdev.regiveapp.item.dto.CreateItemRequest;
import mmdev.regiveapp.item.dto.ItemResponse;
import mmdev.regiveapp.item.dto.UpdateItemRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        ItemResponse created = itemService.create(request);
        return ResponseEntity.created(URI.create("/api/items/" + created.id())).body(created);
    }

    @GetMapping
    public List<ItemResponse> search(@RequestParam(required = false) String city,
                                     @RequestParam(required = false) Long categoryId) {
        return itemService.search(city, categoryId);
    }

    @GetMapping("/{id}")
    public ItemResponse findById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody UpdateItemRequest request) {
        return itemService.update(id, request);
    }

    @PostMapping("/{id}/claim")
    public ItemResponse claim(@PathVariable Long id) {
        return itemService.claim(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
