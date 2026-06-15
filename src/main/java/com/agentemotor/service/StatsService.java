package com.agentemotor.service;

import com.agentemotor.dto.DashboardStatsDTO;

public interface StatsService {

    DashboardStatsDTO getDashboardStats(Long advisorId);
}
