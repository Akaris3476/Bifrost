package cdv.bifrost.auth.controller;

import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.support.RegisterRequestBuilder;
import cdv.bifrost.support.BifrostMockMvcRequestBuilders;
import cdv.bifrost.support.BifrostMockResultMatchers;
import cdv.bifrost.support.BifrostTestData;
import cdv.bifrost.support.IntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static cdv.bifrost.support.BifrostMockResultMatchers.status;

class AuthRegistrationEndToEndTest extends IntegrationTestBase {

    private static final String ENDPOINT = "/auth/register";

    @Test
    void givenValidRegisterRequest_whenRegistering_thenUserIsPersisted() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("register-ok@bifrost.local")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isOk(),
                BifrostMockResultMatchers.success(true)
        );

        // Verify
        var persisted = userRepository.findByEmailIgnoreCase("register-ok@bifrost.local");
        Assertions.assertTrue(persisted.isPresent());
        Assertions.assertEquals(UserStatus.ACTIVE, persisted.get().getStatus());
        Assertions.assertNotEquals(BifrostTestData.DEFAULT_PASSWORD, persisted.get().getPasswordHash());
        Assertions.assertNotNull(persisted.get().getPasswordHash());
    }

    @Test
    void givenRegisterRequestWithExistingEmail_whenRegistering_thenReturnHttp409Conflict() throws Exception {

        // Initialize
        performWith(BifrostMockMvcRequestBuilders.post(ENDPOINT, null,
                RegisterRequestBuilder.valid().withEmail("dup@bifrost.local").build()));

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("dup@bifrost.local")
                .withDisplayName("Second Display")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isConflict(),
                BifrostMockResultMatchers.success(false),
                BifrostMockResultMatchers.errorCode("USER_ALREADY_EXISTS")
        );
    }

    @Test
    void givenRegisterRequestWithMismatchedPasswordConfirmation_whenRegistering_thenReturnHttp400BadRequest() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("mismatch@bifrost.local")
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .withPasswordConfirmation("DifferentPassword1")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isBadRequest(),
                BifrostMockResultMatchers.success(false),
                BifrostMockResultMatchers.errorCode("PASSWORD_MISMATCH")
        );

        // Verify
        Assertions.assertTrue(userRepository.findByEmailIgnoreCase("mismatch@bifrost.local").isEmpty());
    }

    @Test
    void givenRegisterRequestWithBlankDisplayName_whenRegistering_thenReturnHttp400BadRequest() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("blank-name@bifrost.local")
                .withDisplayName("")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isBadRequest()
        );
    }

    @Test
    void givenRegisterRequestWithInvalidEmail_whenRegistering_thenReturnHttp400BadRequest() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("not-an-email")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isBadRequest()
        );
    }

    @Test
    void givenRegisterRequestWithShortPassword_whenRegistering_thenReturnHttp400BadRequest() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("short-pw@bifrost.local")
                .withPassword(BifrostTestData.SHORT_PASSWORD)
                .withPasswordConfirmation(BifrostTestData.SHORT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isBadRequest()
        );
    }

    @Test
    void givenRegisterRequestWithLongPassword_whenRegistering_thenReturnHttp400BadRequest() throws Exception {

        // Given
        var request = RegisterRequestBuilder.valid()
                .withEmail("long-pw@bifrost.local")
                .withPassword(BifrostTestData.LONG_PASSWORD)
                .withPasswordConfirmation(BifrostTestData.LONG_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(ENDPOINT, null, request),
                status().isBadRequest()
        );
    }
}
