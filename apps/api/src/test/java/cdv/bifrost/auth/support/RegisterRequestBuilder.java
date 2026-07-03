package cdv.bifrost.auth.support;

import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.support.BifrostTestData;

public final class RegisterRequestBuilder {

    private String displayName = BifrostTestData.DEFAULT_DISPLAY_NAME;
    private String email = BifrostTestData.DEFAULT_EMAIL;
    private String password = BifrostTestData.DEFAULT_PASSWORD;
    private String passwordConfirmation = BifrostTestData.DEFAULT_PASSWORD_CONFIRMATION;

    private RegisterRequestBuilder() {
    }

    public static RegisterRequestBuilder builder() {
        return new RegisterRequestBuilder();
    }

    public static RegisterRequestBuilder valid() {
        return new RegisterRequestBuilder();
    }

    public RegisterRequestBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public RegisterRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public RegisterRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public RegisterRequestBuilder withPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
        return this;
    }

    public RegisterRequest build() {
        RegisterRequest request = new RegisterRequest();
        request.setDisplayName(displayName);
        request.setEmail(email);
        request.setPassword(password);
        request.setPasswordConfirmation(passwordConfirmation);
        return request;
    }
}
