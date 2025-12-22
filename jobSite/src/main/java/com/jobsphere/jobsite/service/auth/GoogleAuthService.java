
package com.jobsphere.jobsite.service.auth;

import com.jobsphere.jobsite.config.security.JwtTokenProvider;
import com.jobsphere.jobsite.constant.UserType;
import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.auth.OAuthAccount;
import com.jobsphere.jobsite.repository.UserRepository;
import com.jobsphere.jobsite.repository.auth.OAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Map<String, Object> handleGoogleLogin(String email, String name, String googleId) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty()) {
            String otpToken = jwtTokenProvider.createOtpToken(email, null);
            return Map.of(
                    "needsRoleSelection", true,
                    "email", email,
                    "name", name,
                    "otpToken", otpToken,
                    "message", "Please select your role");
        }

        User user = userOpt.get();

        if (user.getUserType() == null) {
            String otpToken = jwtTokenProvider.createOtpToken(email, null);
            return Map.of(
                    "needsRoleSelection", true,
                    "email", email,
                    "name", name,
                    "otpToken", otpToken,
                    "message", "Please select your role");
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.createUserToken(email, user.getUserType().name(), user.getId());

        return Map.of(
                "token", token,
                "email", email,
                "userType", user.getUserType(),
                "needsRoleSelection", false);
    }

    @Transactional
    public Map<String, Object> createUserFromGoogle(String email, String name, String googleId, UserType userType) {
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setGoogleId(googleId);
            user.setUserType(userType);
        } else {
            user = User.builder()
                    .email(email)
                    .googleId(googleId)
                    .userType(userType)
                    .isActive(true)
                    .emailVerified(true)
                    .build();
        }

        userRepository.save(user);

        Optional<OAuthAccount> existingOAuth = oauthRepository.findByProviderAndProviderUserId("GOOGLE", googleId);

        if (existingOAuth.isEmpty()) {
            OAuthAccount oauthAccount = OAuthAccount.builder()
                    .user(user)
                    .provider("GOOGLE")
                    .providerUserId(googleId)
                    .providerEmail(email)
                    .build();
            oauthRepository.save(oauthAccount);
        }

        String token = jwtTokenProvider.createUserToken(email, userType.name(), user.getId());

        return Map.of(
                "token", token,
                "email", email,
                "userType", userType,
                "needsRoleSelection", false);
    }
}
