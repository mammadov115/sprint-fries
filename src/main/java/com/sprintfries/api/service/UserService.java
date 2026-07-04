package com.sprintfries.api.service;

import com.sprintfries.api.entity.User;
import com.sprintfries.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Şifrə BCrypt vasitəsilə təhlükəsiz şəkildə hash-lənir
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}
