package org.demo.bankingtestapi.service;

import org.demo.bankingtestapi.entity.Role;
import org.demo.bankingtestapi.entity.User;
import org.demo.bankingtestapi.exception.BadRequestException;
import org.demo.bankingtestapi.repository.RoleRepository;
import org.demo.bankingtestapi.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User registerUser(String username, String email, String password, String phoneNumber, String nationalId, String address, Date dateOfBirth, Long zipCode,String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BadRequestException("User with this email already exists");
        }
        Role role = roleRepository.findById(Long.valueOf(roleName))
                .orElseThrow(() -> new BadRequestException("Invalid role"));
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setNationalId(nationalId);
        user.setAddress(address);
        user.setDateOfBirth(dateOfBirth);
        user.setZipCode(zipCode);
        user.setRole(role); // Assign role dynamically

        return userRepository.save(user);
    }


    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUsername(userName);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean resetPassword(String userName, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(userName);

        if (userOptional.isEmpty()) {
            return false; // User not found
        }

        User user = userOptional.get();
        user.setPasswordHash(passwordEncoder.encode(newPassword)); // Hash new password
        userRepository.save(user);

        return true;
    }
}
