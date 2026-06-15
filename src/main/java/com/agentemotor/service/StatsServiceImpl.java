package com.agentemotor.service;

import com.agentemotor.dto.DashboardStatsDTO;
import com.agentemotor.model.PolicyStatus;
import com.agentemotor.repository.PolicyRepository;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final PolicyRepository policyRepository;
    private final PolicyService policyService;

    @Override
    @Transactional
    public DashboardStatsDTO getDashboardStats(Long advisorId) {
        policyService.updatePolicyStatuses(advisorId);

        long totalActive = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO).size();
        long expiredWithin30Days = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.VENCIDO).size();
        long expiredBeyond30Days = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.PERDIDO).size();
        long totalRenewed = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.RENOVADA).size();

        long expiringThisWeek = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO)
                .stream()
                .filter(p -> !p.getExpirationDate().isAfter(LocalDate.now().plusDays(AppConstants.URGENT_THRESHOLD_DAYS)))
                .count();

        return DashboardStatsDTO.builder()
                .totalActive((int) totalActive)
                .expiringThisWeek((int) expiringThisWeek)
                .expiringThisMonth((int) (totalActive))
                .expiredWithin30Days((int) expiredWithin30Days)
                .expiredBeyond30Days((int) expiredBeyond30Days)
                .totalRenewed((int) totalRenewed)
                .build();
    }
}
