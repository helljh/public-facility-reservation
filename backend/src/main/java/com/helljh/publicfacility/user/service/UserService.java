package com.helljh.publicfacility.user.service;

import com.helljh.publicfacility.common.exception.BusinessException;
import com.helljh.publicfacility.common.exception.ErrorCode;
import com.helljh.publicfacility.common.security.JwtTokenProvider;
import com.helljh.publicfacility.common.security.TokenHashUtil;
import com.helljh.publicfacility.user.domain.RefreshToken;
import com.helljh.publicfacility.user.domain.User;
import com.helljh.publicfacility.user.dto.*;
import com.helljh.publicfacility.user.repository.RefreshTokenRepository;
import com.helljh.publicfacility.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_EMAIL
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        User user = User.createUser(
                request.email(),
                encodedPassword,
                request.name()
        );

        User savedUser = userRepository.save(user);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_LOGIN
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOGIN
            );
        }

        String accessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name()
                );

        String refreshToken =
                jwtTokenProvider.createRefreshToken(
                        user.getId()
                );

        String refreshTokenHash =
                tokenHashUtil.hash(refreshToken);

        RefreshToken savedRefreshToken =
                RefreshToken.create(
                        user,
                        refreshTokenHash,
                        jwtTokenProvider.getRefreshTokenExpiresAt()
                );

        refreshTokenRepository.save(savedRefreshToken);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {

        String tokenHash =
                tokenHashUtil.hash(request.refreshToken());

        RefreshToken storedToken =
                refreshTokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_REFRESH_TOKEN
                                )
                        );

        if (storedToken.isExpired()) {

            refreshTokenRepository.delete(storedToken);

            throw new BusinessException(
                    ErrorCode.EXPIRED_REFRESH_TOKEN
            );
        }

        User user = storedToken.getUser();

        // 기존 Refresh Token 폐기
        refreshTokenRepository.delete(storedToken);

        // 새로운 Access Token
        String newAccessToken =
                jwtTokenProvider.createAccessToken(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name()
                );

        // 새로운 Refresh Token
        String newRefreshToken =
                jwtTokenProvider.createRefreshToken(
                        user.getId()
                );

        String newRefreshTokenHash =
                tokenHashUtil.hash(newRefreshToken);

        RefreshToken newStoredToken =
                RefreshToken.create(
                        user,
                        newRefreshTokenHash,
                        jwtTokenProvider.getRefreshTokenExpiresAt()
                );

        refreshTokenRepository.save(newStoredToken);

        return new TokenRefreshResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer"
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {

        String tokenHash =
                tokenHashUtil.hash(request.refreshToken());

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(refreshTokenRepository::delete);
    }
}