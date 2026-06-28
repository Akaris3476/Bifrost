package cdv.bifrost.auth.exception;

public class UserAlreadyExist extends RuntimeException {
    public UserAlreadyExist(String email) {
        super("User already exist with email: " + email.toLowerCase() + "!");
    }
}
