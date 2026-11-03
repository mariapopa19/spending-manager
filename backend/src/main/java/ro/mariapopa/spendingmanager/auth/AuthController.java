package ro.mariapopa.spendingmanager.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {
    @GetMapping("/me")
    public MeResponse authMe(Authentication authentication) {
        return new MeResponse(authentication.getName());
    }
}
