package co.unimagdalena.api;

import co.unimagdalena.api.dto.AmenityDto.*;
import co.unimagdalena.services.mapper.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/amenities")
public class AmenityController {
    private final AmenityService amenityService;

    @PostMapping
    public ResponseEntity<AmenityResponse> create(@Validated @RequestBody AmenityCreateRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        var amenityCreated = amenityService.createAmenity(request);
        var location = uriBuilder.path("/api/v1/amenities/{id}").
                buildAndExpand(amenityCreated.id()).
                toUri();
        return ResponseEntity.created(location).body(amenityCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AmenityResponse> update(@PathVariable Long id,@Validated @RequestBody AmenityUpdateRequest request) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityResponse> get(@PathVariable long id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }
}
