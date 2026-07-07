package mmdev.regiveapp.user;

import jakarta.validation.Valid;
import mmdev.regiveapp.user.dto.CreateUserRequest;
import mmdev.regiveapp.user.dto.UpdateUserRequest;
import mmdev.regiveapp.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request){
        UserResponse created = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/"+created.id()))
                .body(created);
    }
    @GetMapping
    public List<UserResponse> findAll(){
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @Valid @RequestBody UpdateUserRequest request){
        return userService.update(id,request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
