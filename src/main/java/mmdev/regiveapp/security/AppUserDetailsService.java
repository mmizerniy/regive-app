package mmdev.regiveapp.security;

import mmdev.regiveapp.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String email)throws UsernameNotFoundException{
        return userRepository.findByEmail(email)
                .map(user-> User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name())))
                        .build())
                .orElseThrow(()->new UsernameNotFoundException("User not found: " + email));
    }
}
