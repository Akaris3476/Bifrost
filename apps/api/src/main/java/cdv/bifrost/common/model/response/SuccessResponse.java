package cdv.bifrost.common.model.response;

import lombok.Getter;

@Getter
public class SuccessResponse<T> {

    // TODO: add request id for log debugging
    
    private final boolean success = true;
    private final T data;

    private SuccessResponse(T data) {
        this.data = data;
    }

    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data);
    }

    public static SuccessResponse<Void> ok() {
        return new SuccessResponse<>(null);
    }
}
