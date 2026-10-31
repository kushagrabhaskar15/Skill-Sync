package com.skillsync.auth;

import com.skillsync.common.MapperUtils;
import com.skillsync.dto.AuthResponse;
import com.skillsync.dto.LoginRequest;
import com.skillsync.dto.RegisterRequest;
import com.skillsync.enums.UserStatus;
import com.skillsync.exception.BadRequestException;
import com.skillsync.exception.ConflictException;
import com.skillsync.security.JwtService;
import com.skillsync.user.User;
import com.skillsync.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().toLowerCase().trim())
                .passwordHash(hashPassword(request.password()))
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        return buildAuthResponse(user);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public String createToken(User user) {
        return jwtService.createToken(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                createToken(user),
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                MapperUtils.toUserSummary(user)
        );
    }
}
