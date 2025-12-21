
package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account is inactive");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getUserType() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
        }

        return new User(
                user.getEmail(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                authorities);
    }
}
