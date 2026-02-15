package com.university.portal.service;

import com.university.portal.dto.UserCreateRequest;
import com.university.portal.dto.UserDTO;
import com.university.portal.dto.UserUpdateRequest;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String name, String email, String password, UserRole role) {
        validateUniqueEmail(email, null);
        validateName(name);

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public User createUser(UserCreateRequest request) {
        return registerUser(request.getName(), request.getEmail(), request.getPassword(), request.getRole());
    }

    public User updateUser(Long id, UserUpdateRequest request) {
        User user = getUserById(id);
        validateName(request.getName());
        validateUniqueEmail(request.getEmail(), id);

        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public long countByRole(UserRole role) {
        return userRepository.findByRole(role).size();
    }

    private void validateUniqueEmail(String email, Long currentUserId) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new BadRequestException("Email cannot be empty");
        }

        userRepository.findByEmailIgnoreCase(normalized).ifPresent(existing -> {
            if (currentUserId == null || !existing.getId().equals(currentUserId)) {
                throw new BadRequestException("Email already exists: " + normalized);
            }
        });
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new BadRequestException("Name cannot be empty");
        }
    }
}
