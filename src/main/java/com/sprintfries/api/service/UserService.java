package com.sprintfries.api.service;

import com.sprintfries.api.entity.User;
import com.sprintfries.api.exception.AccountLockedException;
import com.sprintfries.api.exception.BadCredentialsException;
import com.sprintfries.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION_MINUTES = 10;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User registerUser(User user) {
        // Validation: E-poçt yoxlanılır
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("E-poçt ünvanı boş ola bilməz!");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Şifrə boş ola bilməz!");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Bu e-poçt ünvanı ilə artıq istifadəçi qeydiyyatdan keçib!");
        }

        // İlk istifadəçi yoxlanılır – əgər cədvəl boşdursa "Owner" rolunu alır
        long userCount = userRepository.count();
        if (userCount == 0) {
            user.setRole("Owner");
        } else {
            user.setRole("User");
        }

        // Şifrə BCrypt vasitəsilə təhlükəsiz şəkildə hash-ləyir
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AccountLockedException.class})
    public String login(String email, String password, String tenantId) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("E-poçt ünvanı boş ola bilməz!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Şifrə boş ola bilməz!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("E-poçt və ya şifrə yanlışdır!"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            LocalDateTime lockTime = user.getLockTime();
            if (lockTime != null) {
                long minutesPassed = ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
                if (minutesPassed >= LOCK_TIME_DURATION_MINUTES) {
                    // Lock has expired, unlock account
                    user.setAccountLocked(false);
                    user.setFailedAttempts(0);
                    user.setLockTime(null);
                    userRepository.save(user);
                } else {
                    long remainingMinutes = LOCK_TIME_DURATION_MINUTES - minutesPassed;
                    throw new AccountLockedException("Hesabınız müvəqqəti olaraq kilidlənib!", remainingMinutes);
                }
            }
        }

        // Verify password
        if (passwordEncoder.matches(password, user.getPassword())) {
            // Password matches, reset attempts if any
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                user.setAccountLocked(false);
                user.setLockTime(null);
                userRepository.save(user);
            }
            // Generate JWT
            return jwtService.generateToken(user.getEmail(), user.getRole(), tenantId);
        } else {
            // Password does not match
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                throw new AccountLockedException("Hesabınız 3 uğursuz cəhd səbəbindən kilidləndi! Zəhmət olmasa 10 dəqiqə sonra yenidən cəhd edin.", LOCK_TIME_DURATION_MINUTES);
            } else {
                userRepository.save(user);
                int remainingAttempts = MAX_FAILED_ATTEMPTS - attempts;
                throw new BadCredentialsException("E-poçt və ya şifrə yanlışdır! Qalan cəhd sayı: " + remainingAttempts);
            }
        }
    }
}
