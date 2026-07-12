package mmdev.regiveapp.notification;

import mmdev.regiveapp.notification.dto.NotificationResponse;
import mmdev.regiveapp.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository, CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    public List<NotificationResponse> findMyNotifications(){
        Long userId = currentUserService.getCurrentUser().getId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(n->new NotificationResponse(
                        n.getId(),
                        n.getItem().getId(),
                        n.getItem().getTitle(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt()))
                .collect(Collectors.toList());
    }

}
