package com.sprintfries.api.service;

import com.sprintfries.api.config.TenantContext;
import com.sprintfries.api.entity.User;
import com.sprintfries.api.exception.AccountLockedException;
import com.sprintfries.api.exception.BadCredentialsException;
import com.sprintfries.api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_TENANT = "tenant_bakutech";
    private static final String TEST_EMAIL = "test_login_user@bakutech.com";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeEach
    public void setUp() {
        TenantContext.setCurrentTenant(TEST_TENANT);
        // Clear any existing test user
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        
        // Register a new test user
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setFullName("Test User");
        userService.registerUser(user);
    }

    @AfterEach
    public void tearDown() {
        TenantContext.setCurrentTenant(TEST_TENANT);
        userRepository.findByEmail(TEST_EMAIL).ifPresent(userRepository::delete);
        TenantContext.clear();
    }

    @Test
    public void testSuccessfulLogin() {
        String token = userService.login(TEST_EMAIL, TEST_PASSWORD, TEST_TENANT);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testFailedLoginIncrementsAttemptsAndLocks() {
        // First failed attempt
        assertThrows(BadCredentialsException.class, () -> 
            userService.login(TEST_EMAIL, "wrong_password", TEST_TENANT)
        );
        
        User userAfter1 = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(1, userAfter1.getFailedAttempts());
        assertFalse(userAfter1.isAccountLocked());

        // Second failed attempt
        assertThrows(BadCredentialsException.class, () -> 
            userService.login(TEST_EMAIL, "wrong_password", TEST_TENANT)
        );
        
        User userAfter2 = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(2, userAfter2.getFailedAttempts());
        assertFalse(userAfter2.isAccountLocked());

        // Third failed attempt -> Lockout!
        assertThrows(AccountLockedException.class, () -> 
            userService.login(TEST_EMAIL, "wrong_password", TEST_TENANT)
        );
        
        User userAfter3 = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertEquals(3, userAfter3.getFailedAttempts());
        assertTrue(userAfter3.isAccountLocked());
        assertNotNull(userAfter3.getLockTime());

        // Subsequent login attempt while locked
        AccountLockedException exception = assertThrows(AccountLockedException.class, () -> 
            userService.login(TEST_EMAIL, TEST_PASSWORD, TEST_TENANT)
        );
        assertEquals(10, exception.getRemainingMinutes());
    }

    @Test
    public void testLockoutExpiryUnlocksAccount() {
        // Manually lock user and set lock time to 11 minutes ago
        User user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        user.setAccountLocked(true);
        user.setFailedAttempts(3);
        user.setLockTime(LocalDateTime.now().minusMinutes(11));
        userRepository.save(user);

        // Attempting to log in with correct password should unlock and succeed
        String token = userService.login(TEST_EMAIL, TEST_PASSWORD, TEST_TENANT);
        assertNotNull(token);

        User unlockedUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertFalse(unlockedUser.isAccountLocked());
        assertEquals(0, unlockedUser.getFailedAttempts());
        assertNull(unlockedUser.getLockTime());
    }
}
