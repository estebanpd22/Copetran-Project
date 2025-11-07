package co.unimagdalena.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class ConfigDto {

    public record ConfigCreateRequest(
            @NotBlank String key,
            @NotBlank String value
    ) implements Serializable {}

    public record ConfigUpdateRequest(
            String key,
            @NotBlank String value
    ) implements Serializable {}

    public record ConfigResponse(
            Long id,
            String key,
            String value
    ) implements Serializable {}
}
