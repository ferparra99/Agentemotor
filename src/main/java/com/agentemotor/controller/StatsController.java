package com.agentemotor.controller;

import com.agentemotor.controller.api.StatsApi;
import com.agentemotor.dto.DashboardStatsDTO;
import com.agentemotor.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class StatsController implements StatsApi {

    private final StatsService statsService;

    @Override
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getByAdvisor(@RequestParam Long advisorId) {
        return ResponseEntity.ok(statsService.getDashboardStats(advisorId));
    }
}
