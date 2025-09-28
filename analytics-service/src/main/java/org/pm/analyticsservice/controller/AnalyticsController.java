package org.pm.analyticsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.pm.analyticsservice.dto.AnalyticsResponseDto;
import org.pm.analyticsservice.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Get aggregated analytics stats", description = "Retrieve analytics stats like total patients, new patients, and average age for a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved analytics stats"),
            @ApiResponse(responseCode = "404", description = "No data found for the range"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameters")
    })
    @GetMapping("/stats")
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsStats(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2023-01-01")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2023-12-31")
            @RequestParam(required = false) LocalDate toDate) {
        if(fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("fromDate must be before or equal to toDate");
            }
        }
        AnalyticsResponseDto stats = analyticsService.getAnalyticsStats(fromDate, toDate);
        return ResponseEntity.ok(stats);
    }
}
