package co.unimagdalena.api;

import co.unimagdalena.api.ConfigController;
import co.unimagdalena.api.dto.ConfigDto.*;
import co.unimagdalena.config.TestSecurityConfig;
import co.unimagdalena.services.ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
@Import(TestSecurityConfig.class)
class ConfigControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean ConfigService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new ConfigCreateRequest("MAX_SEATS", "50");
        var resp = new ConfigResponse(1L, "MAX_SEATS", "50");

        when(service.createConfig(any())).thenReturn(resp);

        mvc.perform(post("/api/v1/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/configs/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.key").value("MAX_SEATS"));
    }

    @Test
    void update_shouldReturn204() throws Exception {
        var req = new ConfigUpdateRequest("MAX_SEATS", "60");

        mvc.perform(patch("/api/v1/configs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(service).updateConfig(eq(1L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/v1/configs/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteConfig(1L);
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        var configs = List.of(
                new ConfigResponse(1L, "MAX_SEATS", "50"),
                new ConfigResponse(2L, "MIN_PRICE", "10.00")
        );

        when(service.getAllConfigs()).thenReturn(configs);

        mvc.perform(get("/api/v1/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByKey_shouldReturn200() throws Exception {
        var resp = new ConfigResponse(1L, "MAX_SEATS", "50");

        when(service.getConfigByKey("MAX_SEATS")).thenReturn(resp);

        mvc.perform(get("/api/v1/configs/key/MAX_SEATS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("MAX_SEATS"))
                .andExpect(jsonPath("$.value").value("50"));
    }

    @Test
    void getValueAsString_shouldReturn200() throws Exception {
        when(service.getValueAsString("MAX_SEATS")).thenReturn("50");

        mvc.perform(get("/api/v1/configs/key/MAX_SEATS/string"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("50"));
    }

    @Test
    void getValueAsDecimal_shouldReturn200() throws Exception {
        when(service.getValueAsBigDecimal("MIN_PRICE")).thenReturn(new BigDecimal("10.00"));

        mvc.perform(get("/api/v1/configs/key/MIN_PRICE/decimal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10.00));
    }

    @Test
    void getValueAsInt_shouldReturn200() throws Exception {
        when(service.getValueAsInt("MAX_SEATS")).thenReturn(50);

        mvc.perform(get("/api/v1/configs/key/MAX_SEATS/int"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50));
    }
}
