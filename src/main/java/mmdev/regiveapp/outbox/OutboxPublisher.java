package mmdev.regiveapp.outbox;


import mmdev.regiveapp.event.ItemCreatedEvent;
import mmdev.regiveapp.event.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper=objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending =
                outboxRepository.findByPublishedFalseOrderByCreatedAtAsc(Limit.of(BATCH_SIZE));

        if (pending.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                ItemCreatedEvent payload =
                        objectMapper.readValue(event.getPayload(), ItemCreatedEvent.class);

                kafkaTemplate.send(KafkaTopics.ITEM_CREATED, event.getAggregateId(), payload)
                        .get();

                event.setPublished(true);
                event.setPublishedAt(Instant.now());

                log.info("Published outbox event id={} type={}", event.getId(), event.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}, will retry", event.getId(), e);
            }
        }
    }
}
