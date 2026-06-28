package cdv.bifrost.auth.controller;

import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.service.AuthService;
import cdv.bifrost.common.model.response.SuccessResponse;
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
    public SuccessResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        this.authService.register(request);
        return SuccessResponse.ok();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody @Valid LoginRequest request) {
        // TODO: implement login
        return ResponseEntity.ok().build();
    }

}
