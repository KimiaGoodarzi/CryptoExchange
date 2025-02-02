package com.example.CryptoExchange.rest;

import com.example.CryptoExchange.User;
import com.example.CryptoExchange.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
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

}
