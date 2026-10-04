package com.bank.userservice.service;

import com.bank.userservice.dto.RegisterRequest;
import com.bank.userservice.dto.UserResponse;
import com.bank.userservice.entity.KycStatus;
import com.bank.userservice.entity.User;
import com.bank.userservice.event.UserEventPublisher;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.repository.UserRepository;
import com.bank.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void register_savesUserAndPublishesEvent_whenEmailIsNew() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("supersecret")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            u.setKycStatus(KycStatus.PENDING);
            return u;
        });

        UserResponse response = userService.register(request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getKycStatus()).isEqualTo(KycStatus.PENDING);
        verify(userEventPublisher).publishUserRegistered(any(User.class));
    }

    @Test
    void register_throws_whenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("supersecret")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }
}
