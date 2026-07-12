package mmdev.regiveapp.event;

import mmdev.regiveapp.notification.Notification;
import mmdev.regiveapp.notification.NotificationRepository;
import mmdev.regiveapp.subscription.Subscription;
import mmdev.regiveapp.subscription.SubscriptionRepository;
import mmdev.regiveapp.item.Item;
import mmdev.regiveapp.item.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ItemEventListener {

    private static final Logger log = LoggerFactory.getLogger(ItemEventListener.class);

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final ItemRepository itemRepository;

    public ItemEventListener(SubscriptionRepository subscriptionRepository,
                             NotificationRepository notificationRepository,
                             ItemRepository itemRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.itemRepository = itemRepository;
    }

    @KafkaListener(topics = KafkaTopics.ITEM_CREATED, groupId = "regive-notifications")
    @Transactional
    public void onItemCreated(ItemCreatedEvent event) {
        log.info("Processing ItemCreatedEvent: item={} '{}'", event.itemId(), event.title());

        List<Subscription> matching =
                subscriptionRepository.findMatching(event.categoryId(), event.city());

        if (matching.isEmpty()) {
            log.info("No subscribers for category={} city={}", event.categoryName(), event.city());
            return;
        }

        Item item = itemRepository.findById(event.itemId()).orElse(null);
        if (item == null) {
            log.warn("Item {} no longer exists, skipping notifications", event.itemId());
            return;
        }

        String message = "New item in '%s' (%s): %s"
                .formatted(event.categoryName(), event.city(), event.title());

        List<Notification> notifications = matching.stream()
                .filter(s -> !s.getUser().getId().equals(event.ownerId()))
                .map(s -> {
                    Notification n = new Notification();
                    n.setUser(s.getUser());
                    n.setItem(item);
                    n.setMessage(message);
                    return n;
                })
                .toList();

        notificationRepository.saveAll(notifications);

        log.info(">>> Created {} notifications for item {}", notifications.size(), event.itemId());
    }
}
