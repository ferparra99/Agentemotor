package com.agentemotor.service;

import com.agentemotor.dto.*;
import com.agentemotor.model.*;
import com.agentemotor.repository.AdvisorRepository;
import com.agentemotor.repository.PolicyRepository;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final AdvisorRepository advisorRepository;
    private final ClientService clientService;
    private final ContactAttemptService contactAttemptService;

    @Override
    public void updatePolicyStatuses(Long advisorId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(AppConstants.RENEWAL_WINDOW_DAYS);
        List<Policy> activeOnes = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO);
        int updated = 0;

        for (Policy p : activeOnes) {
            if (p.getExpirationDate().isBefore(thirtyDaysAgo)) {
                p.setStatus(PolicyStatus.PERDIDO);
                updated++;
            } else if (p.getExpirationDate().isBefore(today)) {
                p.setStatus(PolicyStatus.VENCIDO);
                updated++;
            }
        }
        if (!activeOnes.isEmpty()) {
            policyRepository.saveAll(activeOnes);
        }
        if (updated > 0) {
            log.info("Estados actualizados: {} pólizas modificadas", updated);
        }
    }

    @Override
    @Transactional
    public List<PolicySummaryDTO> getPolicies(Long advisorId, String filter) {
        updatePolicyStatuses(advisorId);
        List<Policy> policies = filterPolicies(advisorId, filter);
        return policies.stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    private List<Policy> filterPolicies(Long advisorId, String filter) {
        String f = filter != null ? filter.toLowerCase() : AppConstants.FILTER_ALL;

        if (AppConstants.FILTER_INTERESTED.equals(f) || AppConstants.FILTER_NOT_INTERESTED.equals(f)) {
            ContactAttemptResult targetResult = AppConstants.FILTER_INTERESTED.equals(f)
                    ? ContactAttemptResult.INTERESTED
                    : ContactAttemptResult.NOT_INTERESTED;
            return policyRepository.findByAdvisorIdAndStatusIn(advisorId, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO))
                    .stream()
                    .filter(p -> {
                        Optional<ContactAttempt> last = contactAttemptService.findLastAttemptByPolicy(p.getId());
                        return last.isPresent() && last.get().getResult() == targetResult;
                    })
                    .toList();
        }

        return switch (f) {
            case AppConstants.FILTER_EXPIRING -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO)
                    .stream()
                    .filter(p -> !p.getExpirationDate().isAfter(LocalDate.now().plusDays(AppConstants.RENEWAL_WINDOW_DAYS)))
                    .toList();
            case AppConstants.FILTER_EXPIRED_LT_30 -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.VENCIDO);
            case AppConstants.FILTER_EXPIRED_GT_30 -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.PERDIDO);
            case AppConstants.FILTER_ACTIVE -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO);
            default -> policyRepository.findByAdvisorId(advisorId);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailDTO getPolicyDetail(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.POLICY_NOT_FOUND + policyId));

        Client client = policy.getClient();
        List<ContactAttemptDTO> attemptDTOs = contactAttemptService.getAttemptsByPolicy(policyId);
        int totalAttempts = attemptDTOs.size();

        return buildPolicyDetailDTO(policy, client, attemptDTOs, totalAttempts);
    }

    @Override
    @Transactional
    public PolicySummaryDTO createPolicy(PolicyRequestDTO request) {
        Advisor advisor = advisorRepository.findById(AppConstants.DEFAULT_ADVISOR_ID)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ADVISOR_NOT_FOUND));

        Client client = clientService.findOrCreateClient(
                request.getClientName(),
                request.getClientPhone() != null ? request.getClientPhone() : AppConstants.DEFAULT_PHONE,
                request.getClientEmail(),
                request.getClientNotes(),
                advisor.getId());

        Policy policy = Policy.builder()
                .policyNumber(request.getPolicyNumber())
                .type(PolicyType.valueOf(request.getType()))
                .insurer(request.getInsurer())
                .startDate(request.getStartDate())
                .expirationDate(request.getExpirationDate())
                .status(PolicyStatus.ACTIVO)
                .renewalCount(0)
                .client(client)
                .advisor(advisor)
                .build();

        policy = policyRepository.save(policy);
        return toSummaryDTO(policy);
    }

    @Override
    @Transactional
    public PolicySummaryDTO updatePolicy(PolicyRequestDTO request) {
        Policy policy = policyRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.POLICY_NOT_FOUND + request.getId()));

        if (request.getPolicyNumber() != null) policy.setPolicyNumber(request.getPolicyNumber());
        if (request.getType() != null) policy.setType(PolicyType.valueOf(request.getType()));
        if (request.getInsurer() != null) policy.setInsurer(request.getInsurer());
        if (request.getStartDate() != null) policy.setStartDate(request.getStartDate());
        if (request.getExpirationDate() != null) policy.setExpirationDate(request.getExpirationDate());

        policy = policyRepository.save(policy);
        return toSummaryDTO(policy);
    }

    @Override
    @Transactional
    public PolicySummaryDTO renewPolicy(Long policyId, RenewRequestDTO request) {
        Policy oldPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.POLICY_NOT_FOUND + policyId));

        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(oldPolicy.getExpirationDate(), today);

        if (daysOverdue > 0) {
            if (oldPolicy.getType() != PolicyType.AUTO) {
                throw new IllegalArgumentException(AppConstants.ERROR_RENEW_NOT_AUTO);
            }
            if (daysOverdue > AppConstants.RENEWAL_WINDOW_DAYS) {
                throw new IllegalArgumentException(
                        String.format(AppConstants.ERROR_RENEW_WINDOW_EXPIRED, AppConstants.RENEWAL_WINDOW_DAYS));
            }
        }

        oldPolicy.setStatus(PolicyStatus.RENOVADA);
        policyRepository.save(oldPolicy);

        Policy newPolicy = Policy.builder()
                .policyNumber(oldPolicy.getPolicyNumber() + AppConstants.RENEWAL_POLICY_SUFFIX + (oldPolicy.getRenewalCount() + 1))
                .type(oldPolicy.getType())
                .insurer(oldPolicy.getInsurer())
                .startDate(request.getNewExpirationDate().minusMonths(AppConstants.POLICY_TERM_MONTHS))
                .expirationDate(request.getNewExpirationDate())
                .status(PolicyStatus.ACTIVO)
                .renewalCount(oldPolicy.getRenewalCount() + 1)
                .client(oldPolicy.getClient())
                .advisor(oldPolicy.getAdvisor())
                .build();

        newPolicy = policyRepository.save(newPolicy);
        return toSummaryDTO(newPolicy);
    }

    private PolicySummaryDTO toSummaryDTO(Policy policy) {
        LocalDate today = LocalDate.now();
        Client client = policy.getClient();
        int attempts = contactAttemptService.countAttemptsByPolicy(policy.getId());

        String interestStatus = null;
        String lastContactResult = AppConstants.CONTACT_RESULT_NO_CONTACT;
        Optional<ContactAttempt> lastAttempt = contactAttemptService.findLastAttemptByPolicy(policy.getId());
        if (lastAttempt.isPresent()) {
            ContactAttemptResult r = lastAttempt.get().getResult();
            switch (r) {
                case CONTACTED -> lastContactResult = AppConstants.CONTACT_RESULT_CONTACTED;
                case NO_ANSWER -> lastContactResult = AppConstants.CONTACT_RESULT_NO_ANSWER;
                case LEFT_MESSAGE -> lastContactResult = AppConstants.CONTACT_RESULT_LEFT_MESSAGE;
                case INTERESTED -> {
                    lastContactResult = AppConstants.CONTACT_RESULT_INTERESTED;
                    interestStatus = AppConstants.CONTACT_RESULT_INTERESTED;
                }
                case NOT_INTERESTED -> {
                    lastContactResult = AppConstants.CONTACT_RESULT_NOT_INTERESTED;
                    interestStatus = AppConstants.CONTACT_RESULT_NOT_INTERESTED;
                }
            }
        }

        return PolicySummaryDTO.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .type(policy.getType().name())
                .insurer(policy.getInsurer())
                .clientName(client.getName())
                .clientPhone(client.getPhone())
                .expirationDate(policy.getExpirationDate().toString())
                .daysUntilExpiry(ChronoUnit.DAYS.between(today, policy.getExpirationDate()))
                .daysOverdue(Math.max(0, ChronoUnit.DAYS.between(policy.getExpirationDate(), today)))
                .status(policy.getStatus().name())
                .priority(calculatePriority(policy, today))
                .contactAttempts(attempts)
                .recommendedAction(calculateRecommendedAction(policy, today))
                .interestStatus(interestStatus)
                .lastContactResult(lastContactResult)
                .build();
    }

    private PolicyDetailDTO buildPolicyDetailDTO(Policy policy, Client client,
                                                  List<ContactAttemptDTO> attemptDTOs, int totalAttempts) {
        LocalDate today = LocalDate.now();
        long daysOverdue = Math.max(0, ChronoUnit.DAYS.between(policy.getExpirationDate(), today));
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, policy.getExpirationDate());

        return PolicyDetailDTO.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .type(policy.getType().name())
                .insurer(policy.getInsurer())
                .startDate(policy.getStartDate())
                .expirationDate(policy.getExpirationDate())
                .daysUntilExpiry(daysUntilExpiry)
                .daysOverdue(daysOverdue)
                .status(policy.getStatus().name())
                .priority(calculatePriority(policy, today))
                .recommendedAction(calculateRecommendedAction(policy, today))
                .contactAttempts(totalAttempts)
                .clientId(client.getId())
                .clientName(client.getName())
                .clientPhone(client.getPhone())
                .clientEmail(client.getEmail())
                .contactAttemptList(attemptDTOs)
                .build();
    }

    private String calculatePriority(Policy policy, LocalDate today) {
        if (policy.getStatus() == PolicyStatus.RENOVADA) {
            return AppConstants.PRIORITY_COMPLETADA;
        }
        if (policy.getStatus() == PolicyStatus.PERDIDO) {
            return AppConstants.PRIORITY_PERDIDO;
        }
        if (policy.getStatus() == PolicyStatus.VENCIDO) {
            return AppConstants.PRIORITY_URGENTE;
        }
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, policy.getExpirationDate());
        if (daysUntilExpiry <= AppConstants.URGENT_THRESHOLD_DAYS) {
            return AppConstants.PRIORITY_ALTA;
        }
        if (daysUntilExpiry <= AppConstants.MEDIUM_PRIORITY_DAYS) {
            return AppConstants.PRIORITY_MEDIA;
        }
        return AppConstants.PRIORITY_BAJA;
    }

    private String calculateRecommendedAction(Policy policy, LocalDate today) {
        if (policy.getStatus() == PolicyStatus.RENOVADA || policy.getStatus() == PolicyStatus.PERDIDO) {
            return AppConstants.ACTION_ALREADY_MANAGED;
        }
        if (policy.getStatus() == PolicyStatus.VENCIDO) {
            long daysOverdue = ChronoUnit.DAYS.between(policy.getExpirationDate(), today);
            long remaining = AppConstants.RENEWAL_WINDOW_DAYS - daysOverdue;
            return String.format(AppConstants.ACTION_CONTACT_URGENT, AppConstants.RENEWAL_WINDOW_DAYS, remaining);
        }
        return AppConstants.ACTION_BEFORE_EXPIRY;
    }
}
