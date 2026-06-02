package com.agentemotor.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsDTO {
    private long totalActive;
    private long expiringThisWeek;
    private long expiringThisMonth;
    private long expiredWithin30Days;
    private long expiredBeyond30Days;
    private long totalRenewed;
}
