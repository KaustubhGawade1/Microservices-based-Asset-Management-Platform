package com.microservice.auth_service.security.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.microservice.auth_service.model.RefreshToken;
import com.microservice.auth_service.model.Users;
import com.microservice.auth_service.repository.RefreshTokenRepository;
import com.microservice.auth_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    public void createRefreshToken_whenTokenExists_shouldDeleteOldAndCreateNew() {
        // Arrange
        Long userId = 1L;
        Long durationMs = 60000L;
        Users user = new Users();
        ReflectionTestUtils.setField(user, "user_id", userId);

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", durationMs);

        RefreshToken oldToken = new RefreshToken();
        oldToken.setId(100L);
        oldToken.setUser(user);
        oldToken.setToken("old-token-uuid");

        // Mock userRepository to return the user
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Mock findByUser to return the old token
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(oldToken));

        // Mock save to return a new token
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken savedToken = invocation.getArgument(0);
            savedToken.setId(200L); // simulate DB id generation
            return savedToken;
        });

        // Act
        RefreshToken newToken = refreshTokenService.createRefreshToken(userId);

        // Assert
        // Verify that delete was called on the old token
        verify(refreshTokenRepository, times(1)).delete(oldToken);
        verify(refreshTokenRepository, times(1)).flush();

        // Verify that save was called
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));

        assertNotNull(newToken);
        assertEquals(user, newToken.getUser());
    }
}
