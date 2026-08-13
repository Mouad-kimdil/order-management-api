package com.mouad.order_management_api.auth.service;

import com.mouad.order_management_api.auth.dto.LoginRequest;
import com.mouad.order_management_api.auth.dto.LoginResponse;
import com.mouad.order_management_api.auth.dto.RegisterRequest;
import com.mouad.order_management_api.auth.dto.UserResponse;
import com.mouad.order_management_api.auth.model.RefreshToken;
import com.mouad.order_management_api.auth.model.Role;
import com.mouad.order_management_api.auth.model.User;
import com.mouad.order_management_api.auth.repository.RefreshTokenRepository;
import com.mouad.order_management_api.auth.repository.UserRepository;
import com.mouad.order_management_api.auth.security.JwtService;
import com.mouad.order_management_api.auth.security.SecurityUser;
import com.mouad.order_management_api.common.exception.ConflictException;
import com.mouad.order_management_api.common.exception.InvalidRefreshTokenException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("username already taken: " + request.username());
        }

        User user = new User(
                request.email().toLowerCase(),
                request.username().toLowerCase(),
                passwordEncoder.encode(request.password()),
                Role.ROLE_USER
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username().toLowerCase(),
                        request.password()
                )
        );
        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        refreshTokenRepository.save(new RefreshToken(
                RefreshToken.hash(refreshToken), principal.getUser(),
                jwtService.extractExpiration(refreshToken), false,
                UUID.randomUUID()
        ));
        return new LoginResponse(accessToken, refreshToken, UserResponse.from(principal.getUser()));
    }

    private void revokeFamily(UUID familyId) {
        refreshTokenRepository.revokeAllByFamilyId(familyId);
    }

    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }
        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(RefreshToken.hash(refreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (stored.isRevoked()) {
            revokeFamily(stored.getFamilyId());
            throw new InvalidRefreshTokenException();
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        SecurityUser principal = new SecurityUser(stored.getUser());
        String newAccess = jwtService.generateAccessToken(principal);
        String newRefresh = jwtService.generateRefreshToken(principal);
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        refreshTokenRepository.save(new RefreshToken(
                RefreshToken.hash(newRefresh), stored.getUser(),
                jwtService.extractExpiration(newRefresh), false,
                stored.getFamilyId()
        ));
        return new LoginResponse(newAccess, newRefresh, UserResponse.from(stored.getUser()));
    }

}
