package mmdev.regiveapp.user;

import mmdev.regiveapp.common.exception.DuplicateResourceException;
import mmdev.regiveapp.common.exception.ResourceNotFoundException;
import mmdev.regiveapp.user.dto.CreateUserRequest;
import mmdev.regiveapp.user.dto.UpdateUserRequest;
import mmdev.regiveapp.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("create: saves user and returns response")
    void create_shouldSaveUser(){
        CreateUserRequest request = new CreateUserRequest("Maks","maks@example.com","secret123");
        when(userRepository.existsByEmail("maks@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv->{
            User saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        UserResponse response = userService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Maks");
        assertThat(response.email()).isEqualTo("maks@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("maks@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$hashed");
        assertThat(captor.getValue().getPassword()).isNotEqualTo("secret123");
    }
    @Test
    @DisplayName("create: throws when email already exists")
    void create_shouldThrowOnDuplicateEmail(){
        CreateUserRequest request = new CreateUserRequest("Maks","maks@example.com","secret123");
        when(userRepository.existsByEmail("maks@example.com")).thenReturn(true);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashed");
        assertThatThrownBy(()->userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("maks@example.com");

        verify(userRepository,never()).save(any());
    }

    @Test
    @DisplayName("findById: throws when user does not exist")
    void findById_shouldThrowWhenNotFound(){
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(()->userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("id=99");
    }
    @Test
    @DisplayName("update: mutates managed entity without calling save (dirty checking)")
    void update_shouldRelyOnDirtyChecking(){
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old name");
        existing.setEmail("maks@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        UserResponse response = userService.update(1L,new UpdateUserRequest("New name"));

        assertThat(response.name()).isEqualTo("New name");
        assertThat(existing.getName()).isEqualTo("New name");

        verify(userRepository,never()).save(any());
    }

}
