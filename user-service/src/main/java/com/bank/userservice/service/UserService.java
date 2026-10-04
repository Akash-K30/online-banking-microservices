package com.bank.userservice.service;

import com.bank.userservice.dto.KycStatusUpdateRequest;
import com.bank.userservice.dto.LoginRequest;
import com.bank.userservice.dto.LoginResponse;
import com.bank.userservice.dto.RegisterRequest;
import com.bank.userservice.dto.UserMapper;
import com.bank.userservice.dto.UserResponse;
import com.bank.userservice.entity.KycStatus;
import com.bank.userservice.entity.User;
import com.bank.userservice.event.UserEventPublisher;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.exception.InvalidCredentialsException;
import com.bank.userservice.exception.UserNotFoundException;
import com.bank.userservice.repository.UserRepository;
import com.bank.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserEventPublisher userEventPublisher;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .kycStatus(KycStatus.PENDING)
                .build();

        User saved = userRepository.save(user);
        userEventPublisher.publishUserRegistered(saved);

        return UserMapper.toResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtTokenProvider.getExpirationMs())
                .build();
    }

    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateKycStatus(UUID id, KycStatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));

        user.setKycStatus(request.getKycStatus());
        User saved = userRepository.save(user);
        userEventPublisher.publishKycStatusUpdated(saved);

        return UserMapper.toResponse(saved);
    }
}
