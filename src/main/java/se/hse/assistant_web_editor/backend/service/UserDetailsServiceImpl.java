package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.hse.assistant_web_editor.backend.entity.UserEntity;
import se.hse.assistant_web_editor.backend.repository.UserRepository;

import java.util.ArrayList;

/// Custom implementation of UserDetailsService.
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /// Get user details by username (used for jwt tokens).
    ///
    /// @param username Username.
    /// @return User details info.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(user.getUsername(), user.getPasswordHash(), new ArrayList<>());
    }
}
