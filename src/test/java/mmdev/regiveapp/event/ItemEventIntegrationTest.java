package mmdev.regiveapp.event;

import mmdev.regiveapp.AbstractIntegrationTest;
import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.item.ItemService;
import mmdev.regiveapp.item.dto.CreateItemRequest;
import mmdev.regiveapp.notification.Notification;
import mmdev.regiveapp.notification.NotificationRepository;
import mmdev.regiveapp.outbox.OutboxRepository;
import mmdev.regiveapp.subscription.Subscription;
import mmdev.regiveapp.subscription.SubscriptionRepository;
import mmdev.regiveapp.user.Role;
import mmdev.regiveapp.user.User;
import mmdev.regiveapp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ItemEventIntegrationTest extends AbstractIntegrationTest {


    @Autowired private ItemService itemService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private OutboxRepository outboxRepository;

    private User owner;
    private User subscriber;
    private Category category;

    @BeforeEach
    void setUp() {
        owner = createUser("owner-" + System.nanoTime() + "@example.com");
        subscriber = createUser("subscriber-" + System.nanoTime() + "@example.com");

        category = new Category();
        category.setName("Cat-" + System.nanoTime());
        category = categoryRepository.save(category);

        Subscription subscription = new Subscription();
        subscription.setUser(subscriber);
        subscription.setCategory(category);
        subscription.setCity(null);
        subscriptionRepository.save(subscription);

        authenticateAs(owner);
    }

    @Test
    @DisplayName("Creating an item eventually notifies subscribers via outbox + Kafka")
    void createItem_shouldNotifySubscribersAsynchronously() {
        var request = new CreateItemRequest(
                "Стара клавіатура", "Робоча", null, "Lviv", category.getId());
        var created = itemService.create(request);

        assertThat(outboxRepository.findAll())
                .anyMatch(e -> e.getAggregateId().equals(String.valueOf(created.id())));

        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<Notification> notifications =
                            notificationRepository.findByUserIdOrderByCreatedAtDesc(subscriber.getId());

                    assertThat(notifications).hasSize(1);
                    assertThat(notifications.getFirst().getItem().getId()).isEqualTo(created.id());
                    assertThat(notifications.getFirst().getMessage()).contains("Стара клавіатура");
                });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(outboxRepository.findAll())
                        .filteredOn(e -> e.getAggregateId().equals(String.valueOf(created.id())))
                        .allMatch(e -> e.isPublished()));
    }

    @Test
    @DisplayName("Owner does not get notified about their own item")
    void createItem_shouldNotNotifyOwner() {
        Subscription ownerSub = new Subscription();
        ownerSub.setUser(owner);
        ownerSub.setCategory(category);
        subscriptionRepository.save(ownerSub);

        var created = itemService.create(new CreateItemRequest(
                "Диван", null, null, "Kyiv", category.getId()));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(subscriber.getId()))
                        .hasSize(1));

        assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(owner.getId()))
                .isEmpty();
    }

    private User createUser(String email){
        User user = new User();
        user.setName("Test");
        user.setEmail(email);
        user.setPassword("hashed");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private void authenticateAs(User user){
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities())
        );
    }
}
