package mmdev.regiveapp.item;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.item.dto.CreateItemRequest;
import mmdev.regiveapp.item.dto.ItemResponse;
import mmdev.regiveapp.outbox.OutboxService;
import mmdev.regiveapp.security.CurrentUserService;
import mmdev.regiveapp.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private OutboxService outboxService;
    @Mock private MeterRegistry meterRegistry;


    private ItemService itemService;

    private User owner;

    private Category category;

    @BeforeEach
    void setUp(){
        owner = new User();
        owner.setId(1L);
        owner.setName("Maks");
        owner.setEmail("maks@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Electronic");

        itemService = new ItemService(
                itemRepository,
                categoryRepository,
                currentUserService,
                outboxService,
                new SimpleMeterRegistry());
    }

    private Item activeItem(){
        Item item = new Item();
        item.setId(10L);
        item.setTitle("Old keyboard");
        item.setCity("Ivano-Frankivsk");
        item.setStatus(ItemStatus.ACTIVE);
        item.setOwner(owner);
        item.setCategory(category);
        return item;
    }
    @Test
    @DisplayName("claim: reserves an ACTIVE item")
    void claim_shouldReserveActiveItem() {
        Item item = activeItem();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.claim(10L);

        assertThat(response.status()).isEqualTo(ItemStatus.RESERVED);
        assertThat(item.getStatus()).isEqualTo(ItemStatus.RESERVED);
    }

    @Test
    @DisplayName("claim: throws when item is already reserved")
    void claim_shouldThrowWhenNotActive() {
        Item item = activeItem();
        item.setStatus(ItemStatus.RESERVED);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.claim(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    @DisplayName("create: maps request to entity and flattens relations in response")
    void create_shouldCreateItem() {
        CreateItemRequest request = new CreateItemRequest(
                "Old keyboard", "Is worked", new BigDecimal("150.00"), "Ivano-Frankivsk", 1L);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item saved = inv.getArgument(0);
            saved.setId(10L);
            saved.setStatus(ItemStatus.ACTIVE);
            return saved;
        });

        ItemResponse response = itemService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.price()).isEqualByComparingTo("150.00");
        assertThat(response.ownerName()).isEqualTo("Maks");
        assertThat(response.categoryName()).isEqualTo("Electronic");
        assertThat(response.status()).isEqualTo(ItemStatus.ACTIVE);
    }
}
