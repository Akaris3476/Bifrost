package cdv.bifrost.auth.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Password and password confirmation do not match");
    }
}
