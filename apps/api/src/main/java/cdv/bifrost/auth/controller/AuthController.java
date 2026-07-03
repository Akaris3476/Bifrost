package cdv.bifrost.auth.controller;

import cdv.bifrost.auth.constant.SessionConstant;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.model.response.AuthUserResponse;
import cdv.bifrost.auth.service.AuthService;
import cdv.bifrost.common.model.response.SuccessResponse;
import cdv.bifrost.common.security.Identity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
class AuthController {

    private final AuthService authService;
    private final Identity identity;

    @PostMapping("/register")
    public SuccessResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return SuccessResponse.ok();
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<Void>> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String rawSessionId = authService.login(
                request,
                clientIp(httpRequest),
                userAgent(httpRequest)
        );
        writeSessionCookie(httpResponse, rawSessionId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @CookieValue(name = SessionConstant.SESSION_COOKIE_NAME, required = false) String rawSessionId,
            HttpServletResponse httpResponse
    ) {
        authService.logout(rawSessionId);
        expireSessionCookie(httpResponse);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping("/me")
    public SuccessResponse<AuthUserResponse> me() {
        User user = authService.me(identity.getSessionHash());
        return SuccessResponse.of(AuthUserResponse.builder()
                .id(java.util.UUID.fromString(user.getId()))
                .email(user.getEmail())
                .displayName(user.getName())
                .status(user.getStatus())
                .build());
    }

    private static void writeSessionCookie(HttpServletResponse response, String value) {
        ResponseCookie cookie = ResponseCookie.from(SessionConstant.SESSION_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(SessionConstant.SESSION_TTL)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static void expireSessionCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(SessionConstant.SESSION_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        return header != null && header.length() > 255 ? header.substring(0, 255) : header;
    }
}
