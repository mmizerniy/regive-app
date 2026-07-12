package mmdev.regiveapp.item;

import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import mmdev.regiveapp.item.dto.CreateItemRequest;
import mmdev.regiveapp.item.dto.ItemResponse;
import mmdev.regiveapp.item.dto.UpdateItemRequest;
import mmdev.regiveapp.security.CurrentUserService;
import mmdev.regiveapp.user.Role;
import mmdev.regiveapp.user.User;
import mmdev.regiveapp.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ItemResponse create(CreateItemRequest request){
        User owner = currentUserService.getCurrentUser();
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
    @Cacheable(cacheNames = "items",key = "#id")
    public ItemResponse findById(Long id) {
        return toResponse(getItemOrThrow(id));
    }

    @Transactional
    @CacheEvict(cacheNames = "items",key = "#id")
    public ItemResponse update(Long id, UpdateItemRequest request) {
        Item item = getItemOrThrow(id);
        assertCanModify(item);
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
    @CacheEvict(cacheNames = "items",key = "#id")
    public ItemResponse claim(Long id) {
        Item item = getItemOrThrow(id);
        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new IllegalStateException("Item is not available, current status: " + item.getStatus());
        }
        item.setStatus(ItemStatus.RESERVED);
        return toResponse(item);
    }

    @Transactional
    @CacheEvict(cacheNames = "items",key = "#id")
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
    private void assertCanModify(Item item){
        User current = currentUserService.getCurrentUser();
        boolean isOwner = item.getOwner().getId().equals(current.getId());
        boolean isModerator = current.getRole() == Role.MODERATOR;
        if (!isOwner && !isModerator){
            throw new AccessDeniedException("You can modify only your own items");
        }
    }
}

