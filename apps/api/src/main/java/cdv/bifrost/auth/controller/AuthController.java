package cdv.bifrost.auth.controller;

import cdv.bifrost.auth.exception.UserAlreadyExist;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
class AuthController {

    final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterRequest request) {
        // TODO: implement registration
        // 1. Check if user exists
        if (!this.authService.isUserExist(request.getEmail())) {
            throw new UserAlreadyExist(request.getEmail());
        }
        // 2. create user
        User user = authService.registerUser(request);
        // 4. send a couple of email ( confirmation, and welcome email)

        // 5. create a cookie after registration for the authenticated account.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid LoginRequest request) {
        // TODO: implement login
        return ResponseEntity.ok().build();
    }

}
