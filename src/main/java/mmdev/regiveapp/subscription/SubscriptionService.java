package mmdev.regiveapp.subscription;

import mmdev.regiveapp.subscription.dto.CreateSubscriptionRequest;
import mmdev.regiveapp.subscription.dto.SubscriptionResponse;
import mmdev.regiveapp.category.Category;
import mmdev.regiveapp.category.CategoryRepository;
import mmdev.regiveapp.common.exception.AccessDeniedException;
import mmdev.regiveapp.common.exception.DuplicateResourceException;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import mmdev.regiveapp.security.CurrentUserService;
import mmdev.regiveapp.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.subscriptionRepository = subscriptionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }
    @Transactional
    public SubscriptionResponse subscribe(CreateSubscriptionRequest request){
        User user = currentUserService.getCurrentUser();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Category not found: id=" + request.categoryId()
                ));
        if (subscriptionRepository.existsByUserIdAndCategoryIdAndCity(
                user.getId(), request.categoryId(), request.city()
        )){
            throw new DuplicateResourceException("Already subscribed to this category");
        }
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setCategory(category);
        subscription.setCity(request.city());

        return toResponse(subscriptionRepository.save(subscription));
    }

    public List<SubscriptionResponse> findMySubscriptions(){
        User user = currentUserService.getCurrentUser();
        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public void unsubscribe(Long id){
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Subscription not found: id=" + id));
        User current = currentUserService.getCurrentUser();
        if (!subscription.getUser().getId().equals(current.getId())){
            throw  new AccessDeniedException("You can delete only your own subscriptions");
        }
        subscriptionRepository.delete(subscription);
    }

    private SubscriptionResponse toResponse(Subscription s){
        return new SubscriptionResponse(
                s.getId(),
                s.getCategory().getId(),
                s.getCategory().getName(),
                s.getCity(),
                s.getCreatedAt()
        );
    }

}
