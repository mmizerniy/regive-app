package mmdev.regiveapp.user;

import mmdev.regiveapp.common.exception.DuplicateResourceException;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import mmdev.regiveapp.user.dto.CreateUserRequest;
import mmdev.regiveapp.user.dto.UpdateUserRequest;
import mmdev.regiveapp.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> findAll(){
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(Long id){
        return toResponse(getUserOrThrow(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request){
        User user = getUserOrThrow(id);
        user.setName(request.name());
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id){
        userRepository.delete(getUserOrThrow(id));
    }

    private User getUserOrThrow(Long id){
        return userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("User not found: id=" + id));
    }

    private UserResponse toResponse(User u){
        return new UserResponse(u.getId(),u.getName(), u.getEmail(),u.getCreatedAt());
    }
}
