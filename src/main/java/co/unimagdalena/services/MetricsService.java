package co.unimagdalena.services;

import co.unimagdalena.api.dto.MetricsDto.MetricsResponse;

import java.time.LocalDate;

public interface MetricsService {
    MetricsResponse getMetrics(LocalDate startDate, LocalDate endDate);
}
