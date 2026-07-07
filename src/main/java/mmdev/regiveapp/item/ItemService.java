package mmdev.regiveapp.item;

import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import mmdev.regiveapp.item.dto.CreateItemRequest;
import mmdev.regiveapp.item.dto.ItemResponse;
import mmdev.regiveapp.item.dto.UpdateItemRequest;
import mmdev.regiveapp.user.User;
import mmdev.regiveapp.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ItemResponse create(CreateItemRequest request){
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(()->new ResourceNotFoundException("User not found: id="+request.ownerId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(()->new ResourceNotFoundException("Category not found: id="+request.categoryId()));

        Item item = new Item();
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCity(request.city());
        item.setOwner(owner);
        item.setCategory(category);

        return toResponse(itemRepository.save(item));
    }

    public List<ItemResponse> search(String city, Long categoryId) {
        return itemRepository.search(city, categoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ItemResponse findById(Long id) {
        return toResponse(getItemOrThrow(id));
    }

    @Transactional
    public ItemResponse update(Long id, UpdateItemRequest request) {
        Item item = getItemOrThrow(id);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: id=" + request.categoryId()));

        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCity(request.city());
        item.setCategory(category);
        return toResponse(item);
    }

    @Transactional
    public ItemResponse claim(Long id) {
        Item item = getItemOrThrow(id);
        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new IllegalStateException("Item is not available, current status: " + item.getStatus());
        }
        item.setStatus(ItemStatus.RESERVED);
        return toResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.delete(getItemOrThrow(id));
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: id=" + id));
    }

    private ItemResponse toResponse(Item i) {
        return new ItemResponse(
                i.getId(), i.getTitle(), i.getDescription(), i.getPrice(), i.getCity(),
                i.getStatus(),
                i.getOwner().getId(), i.getOwner().getName(),
                i.getCategory().getId(), i.getCategory().getName(),
                i.getCreatedAt());
    }
}

