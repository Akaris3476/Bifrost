package cdv.bifrost.auth.support;

import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.support.BifrostTestData;

public final class LoginRequestBuilder {

    private String email = BifrostTestData.DEFAULT_EMAIL;
    private String password = BifrostTestData.DEFAULT_PASSWORD;

    private LoginRequestBuilder() {
    }

    public static LoginRequestBuilder valid() {
        return new LoginRequestBuilder();
    }

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public LoginRequestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public LoginRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest build() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
