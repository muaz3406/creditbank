package com.bank.credits.service;

import com.bank.credits.dto.model.UserDTO;
import com.bank.credits.dto.request.UserCreateRequest;
import com.bank.credits.entity.RoleEntity;
import com.bank.credits.entity.UserEntity;
import com.bank.credits.entity.mapper.UserMapper;
import com.bank.credits.enums.UserRole;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.RoleRepository;
import com.bank.credits.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper mapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ApiException(ApiErrorCode.USERNAME_EXISTS.getCode());
        }
        Set<UserRole> roleNames = request.getRoles();
        Set<RoleEntity> roles = new HashSet<>();
        for (UserRole userRole : roleNames) {
            RoleEntity role = roleRepository.findByName(userRole)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.ENTITY_NOT_FOUND.getCode()));
            roles.add(role);
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        return mapper.toDTO(userRepository.save(user));
    }
}

