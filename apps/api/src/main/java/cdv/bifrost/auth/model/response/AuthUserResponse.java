package cdv.bifrost.auth.model.response;

import cdv.bifrost.auth.model.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthUserResponse {

    private UUID id;
    private String email;
    private String displayName;
    private UserStatus status;
}
