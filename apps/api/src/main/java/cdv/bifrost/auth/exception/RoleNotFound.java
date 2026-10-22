package cdv.bifrost.auth.exception;

public class RoleNotFound extends RuntimeException {
    public RoleNotFound(String role) {
        super("Role not found: " + role);
    }
}
