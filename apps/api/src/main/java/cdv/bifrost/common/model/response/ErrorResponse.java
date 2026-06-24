package cdv.bifrost.common.model.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private final String message;
    private final String errorCode;
    private final OffsetDateTime timestamp;

    public static ErrorResponse of(String message, String errorCode) {
        return ErrorResponse.builder()
                .message(message)
                .errorCode(errorCode)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
