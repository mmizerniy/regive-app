package mmdev.regiveapp.subscription;


import jakarta.validation.Valid;
import mmdev.regiveapp.subscription.dto.CreateSubscriptionRequest;
import mmdev.regiveapp.subscription.dto.SubscriptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(
            @Valid @RequestBody CreateSubscriptionRequest request
            ){
        SubscriptionResponse created = subscriptionService.subscribe(request);
        return ResponseEntity.created(URI.create("/api/subscriptions/"+created.id())).body(created);
    }

    @GetMapping
    public List<SubscriptionResponse> findMySubscriptions(){
        return subscriptionService.findMySubscriptions();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id){
        subscriptionService.unsubscribe(id);
        return ResponseEntity.noContent().build();
    }
}
