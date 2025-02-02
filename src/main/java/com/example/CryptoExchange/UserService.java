package com.example.CryptoExchange;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String email, String password){
        if(userRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("Username is already taken.");
        }
        if(userRepository.findByEmail(email).isPresent()){
            throw new RuntimeException("This email address is already registered.");
        }

        String encryptedPassword  = passwordEncoder.encode(password);
        User newUser = new User(username, email, encryptedPassword);
        return userRepository.save(newUser);
    }


}
