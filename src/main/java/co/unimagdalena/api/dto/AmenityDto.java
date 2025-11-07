package co.unimagdalena.api.dto;

import java.io.Serializable;

public class AmenityDto {
    public record AmenityCreateRequest(String name) implements Serializable {}
    public record AmenityResponse(Long id,String name) implements Serializable {}
}
