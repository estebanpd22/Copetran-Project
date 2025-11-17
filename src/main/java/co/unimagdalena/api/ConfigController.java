package co.unimagdalena.api;

import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.services.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/configs")
public class ConfigController {
    private final ConfigService configService;

    @PostMapping
    public ResponseEntity<ConfigResponse> create(@Validated @RequestBody ConfigCreateRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        var configCreated = configService.createConfig(request);
        var location = uriBuilder.path("/api/v1/configs/{id}")
                .buildAndExpand(configCreated.id())
                .toUri();
        return ResponseEntity.created(location).body(configCreated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Validated @RequestBody ConfigUpdateRequest request) {
        configService.updateConfig(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ConfigResponse>> getAll() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<ConfigResponse> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(configService.getConfigByKey(key));
    }

    @GetMapping("/key/{key}/string")
    public ResponseEntity<String> getValueAsString(@PathVariable String key) {
        return ResponseEntity.ok(configService.getValueAsString(key));
    }

    @GetMapping("/key/{key}/decimal")
    public ResponseEntity<BigDecimal> getValueAsDecimal(@PathVariable String key) {
        return ResponseEntity.ok(configService.getValueAsBigDecimal(key));
    }

    @GetMapping("/key/{key}/int")
    public ResponseEntity<Integer> getValueAsInt(@PathVariable String key) {
        return ResponseEntity.ok(configService.getValueAsInt(key));
    }
}
