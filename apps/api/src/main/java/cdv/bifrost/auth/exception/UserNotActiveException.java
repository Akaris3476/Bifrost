package cdv.bifrost.auth.exception;

public class UserNotActiveException extends RuntimeException {
    public UserNotActiveException(String email) {
        super("User is not active: " + email);
    }
}
