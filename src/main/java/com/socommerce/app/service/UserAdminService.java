package com.socommerce.app.service;

import com.socommerce.app.entity.RoleName;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User setActive(Long id, boolean active) {
        User user = get(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public User assignRole(Long id, RoleName role) {
        User user = get(id);
        user.setRole(role);
        return userRepository.save(user);
    }
}
