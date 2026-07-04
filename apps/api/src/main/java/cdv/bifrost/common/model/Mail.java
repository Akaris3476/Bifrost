package cdv.bifrost.common.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class Mail {
    private List<String> to;
    private String subject;
    private String template;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
}
