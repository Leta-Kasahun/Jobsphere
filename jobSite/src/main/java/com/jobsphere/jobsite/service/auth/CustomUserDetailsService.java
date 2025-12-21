
package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.admin.AdminRepository;
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
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Check if it's an admin first (prioritize admin role)
        var adminOpt = adminRepository.findByEmailIgnoreCase(username);
        if (adminOpt.isPresent()) {
            var admin = adminOpt.get();
            if (!admin.isActive()) {
                throw new UsernameNotFoundException("Admin account is inactive");
            }
            return new User(admin.getEmail(), admin.getPasswordHash(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // 2. Check if it's a regular user
        var userOpt = userRepository.findByEmailIgnoreCase(username);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            if (!user.isActive()) {
                throw new UsernameNotFoundException("Account is inactive");
            }
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (user.getUserType() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
            }
            return new User(user.getEmail(), user.getPasswordHash() != null ? user.getPasswordHash() : "", authorities);
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
