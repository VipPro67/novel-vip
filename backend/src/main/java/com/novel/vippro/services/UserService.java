package com.novel.vippro.services;

import com.novel.vippro.models.User;
import com.novel.vippro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsBySupabaseId(String supabaseId) {
        return userRepository.existsBySupabaseId(supabaseId);
    }

    public Optional<User> findBySupabaseId(String supabaseId) {
        return userRepository.findBySupabaseId(supabaseId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
} 