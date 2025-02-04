package com.example.CryptoExchange.rest;

import com.example.CryptoExchange.JwtUtil;
import com.example.CryptoExchange.User;
import com.example.CryptoExchange.UserRepository;
import com.example.CryptoExchange.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserController(UserService userService, JwtUtil jwtUtil, UserRepository userRepository){
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> userData){
        try{
            String username = userData.get("username");
            String email = userData.get("email");
            String password = userData.get("password");

            User newUser = userService.registerUser(username,email, password);
            return ResponseEntity.ok(Map.of("message",  "User registered successfully",
                    "username", newUser.getUsername(),
                    "email", newUser.getEmail()
            ));


        }
        catch(RuntimeException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String,String> loginData){

            String username = loginData.get("username");

            String password = loginData.get("password");

            Optional<User> userOptional = userRepository.findByUsername(username);

            if(userOptional.isPresent()){
                User user = userOptional.get();
                if(passwordEncoder.matches(password, user.getPassword())){
                    String token = jwtUtil.generateToken(username);
                    return ResponseEntity.ok(Map.of("token", token));
                }
            }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
    }

}
