package mmdev.regiveapp.event;

import org.apache.thrift.Logger;
import org.apache.thrift.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class ItemEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ItemEventPublisher.class);
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public ItemEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishItemCreated(ItemCreatedEvent event){
        String key = String.valueOf(event.categoryId());

        kafkaTemplate.send(KafkaTopics.ITEM_CREATED,key,event)
                .whenComplete((result,ex)->{
                   if (ex!=null){
                       log.error("Failed to publish ItemCreatedEvent for item {}",event.itemId());
                   }else{
                       log.info("Published ItemCreatedEvent for item {} to partition {}",
                               event.itemId(),result.getRecordMetadata().partition());
                   }
                });
    }
}
