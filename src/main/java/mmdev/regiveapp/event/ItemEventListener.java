package mmdev.regiveapp.event;

import org.apache.thrift.Logger;
import org.apache.thrift.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class ItemEventListener {

    private static final Logger log = LoggerFactory.getLogger(ItemEventListener.class);

    @KafkaListener(topics = KafkaTopics.ITEM_CREATED,groupId = "regive-notifications")
    public void onItemCreated(ItemCreatedEvent event){
        log.info(">>> CONSUMED ItemCreatedEvent: item={} '{}' ({}), owner={}",
                event.itemId(),event.title(),event.city(),event.categoryName(),event.ownerId());
    }

}
