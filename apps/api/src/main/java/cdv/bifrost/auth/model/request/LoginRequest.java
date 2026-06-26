package cdv.bifrost.auth.model.request;

import cdv.bifrost.auth.constant.PasswordConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = PasswordConstant.MIN_PASSWORD_LENGTH, max = PasswordConstant.MAX_PASSWORD_LENGTH)
    private String password;

}
