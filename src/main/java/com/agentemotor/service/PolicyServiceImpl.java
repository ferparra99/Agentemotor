package com.agentemotor.service;

import com.agentemotor.dto.*;
import com.agentemotor.model.*;
import com.agentemotor.repository.AdvisorRepository;
import com.agentemotor.repository.ClientRepository;
import com.agentemotor.repository.ContactAttemptRepository;
import com.agentemotor.repository.PolicyRepository;
import com.agentemotor.utils.PolicyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final ContactAttemptRepository contactAttemptRepository;
    private final AdvisorRepository advisorRepository;

    @Autowired
    @Lazy
    private PolicyService self;

    @Override
    @Transactional
    public List<PolicySummaryDTO> getPolicies(Long advisorId, String filter) {
        log.debug("Consultando pólizas para advisor {} con filtro {}", advisorId, filter);
        updatePolicyStatuses(advisorId);
        List<Policy> policies = filterPolicies(advisorId, filter);
        log.info("Se encontraron {} pólizas (filtro: {})", policies.size(), filter);
        return policies.stream()
                .map(this::toSummaryDTO)
                .toList();
    }

    private void updatePolicyStatuses(Long advisorId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(PolicyConstants.RENEWAL_WINDOW_DAYS);
        List<Policy> activeOnes = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO);
        int updated = 0;

        for (Policy p : activeOnes) {
            if (p.getExpirationDate().isBefore(thirtyDaysAgo)) {
                p.setStatus(PolicyStatus.PERDIDO);
                updated++;
                log.info("Póliza {} marcada como PERDIDO (venció hace >{} días)", p.getPolicyNumber(), PolicyConstants.RENEWAL_WINDOW_DAYS);
            } else if (p.getExpirationDate().isBefore(today)) {
                p.setStatus(PolicyStatus.VENCIDO);
                updated++;
                log.info("Póliza {} marcada como VENCIDO", p.getPolicyNumber());
            }
        }
        if (!activeOnes.isEmpty()) {
            policyRepository.saveAll(activeOnes);
        }
        if (updated > 0) {
            log.info("Estados actualizados: {} pólizas modificadas", updated);
        }
    }

    private List<Policy> filterPolicies(Long advisorId, String filter) {
        String f = filter != null ? filter.toLowerCase() : "all";

        if ("interested".equals(f) || "not_interested".equals(f)) {
            ContactAttemptResult targetResult = "interested".equals(f)
                    ? ContactAttemptResult.INTERESTED
                    : ContactAttemptResult.NOT_INTERESTED;
            return policyRepository.findByAdvisorIdAndStatusIn(advisorId, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO))
                    .stream()
                    .filter(p -> {
                        Optional<ContactAttempt> last = contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(p.getId());
                        return last.isPresent() && last.get().getResult() == targetResult;
                    })
                    .toList();
        }

        return switch (f) {
            case "expiring" -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO)
                    .stream()
                    .filter(p -> !p.getExpirationDate().isAfter(LocalDate.now().plusDays(30)))
                    .toList();
            case "expired_lt_30" -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.VENCIDO);
            case "expired_gt_30" -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.PERDIDO);
            case "active" -> policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO);
            default -> policyRepository.findByAdvisorId(advisorId);
        };
    }

    @Override
    @Transactional
    public DashboardStatsDTO getDashboardStats(Long advisorId) {
        log.debug("Calculando estadísticas del dashboard para advisor {}", advisorId);
        updatePolicyStatuses(advisorId);

        long totalActive = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO).size();
        long expiredWithin30Days = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.VENCIDO).size();
        long expiredBeyond30Days = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.PERDIDO).size();
        long totalRenewed = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.RENOVADA).size();

        long expiringThisWeek = policyRepository.findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO)
                .stream()
                .filter(p -> !p.getExpirationDate().isAfter(LocalDate.now().plusDays(7)))
                .count();

        log.info("Estadísticas: {} activas, {} vencen esta semana, {} vencidas <30d, {} perdidas, {} renovadas",
                totalActive, expiringThisWeek, expiredWithin30Days, expiredBeyond30Days, totalRenewed);

        return DashboardStatsDTO.builder()
                .totalActive((int) totalActive)
                .expiringThisWeek((int) expiringThisWeek)
                .expiringThisMonth((int) (totalActive))
                .expiredWithin30Days((int) expiredWithin30Days)
                .expiredBeyond30Days((int) expiredBeyond30Days)
                .totalRenewed((int) totalRenewed)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailDTO getPolicyDetail(Long policyId) {
        log.debug("Consultando detalle de póliza {}", policyId);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.warn("Póliza no encontrada: {}", policyId);
                    return new IllegalArgumentException(PolicyConstants.POLICY_NOT_FOUND + policyId);
                });

        Client client = policy.getClient();
        List<ContactAttempt> attempts = contactAttemptRepository.findByPolicyIdOrderByDateDesc(policyId);
        int totalAttempts = attempts.size();

        List<ContactAttemptDTO> attemptDTOs = attempts.stream()
                .map(a -> ContactAttemptDTO.builder()
                        .id(a.getId())
                        .date(a.getDate())
                        .type(a.getType().name())
                        .result(a.getResult().name())
                        .notes(a.getNotes())
                        .build())
                .toList();

        return buildPolicyDetailDTO(policy, client, attemptDTOs, totalAttempts);
    }

    @Override
    @Transactional
    public ContactAttemptDTO registerContactAttempt(ContactAttemptRequestDTO request) {
        log.debug("Registrando gestión para póliza {}: {} → {}", request.getPolicyId(), request.getType(), request.getResult());
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> {
                    log.warn("Póliza no encontrada al registrar gestión: {}", request.getPolicyId());
                    return new IllegalArgumentException(PolicyConstants.POLICY_NOT_FOUND + request.getPolicyId());
                });

        ContactAttempt attempt = ContactAttempt.builder()
                .policy(policy)
                .date(LocalDateTime.now())
                .type(ContactAttemptType.valueOf(request.getType()))
                .result(ContactAttemptResult.valueOf(request.getResult()))
                .notes(request.getNotes())
                .build();

        attempt = contactAttemptRepository.save(attempt);

        log.info("Gestión registrada: {} → {} en póliza {} (ID gestión: {})",
                request.getType(), request.getResult(), policy.getPolicyNumber(), attempt.getId());

        return ContactAttemptDTO.builder()
                .id(attempt.getId())
                .date(attempt.getDate())
                .type(attempt.getType().name())
                .result(attempt.getResult().name())
                .notes(attempt.getNotes())
                .build();
    }

    @Override
    @Transactional
    public PolicySummaryDTO renewPolicy(Long policyId, RenewRequestDTO request) {
        log.debug("Iniciando renovación de póliza {}", policyId);
        Policy oldPolicy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.warn("Póliza no encontrada para renovación: {}", policyId);
                    return new IllegalArgumentException(PolicyConstants.POLICY_NOT_FOUND + policyId);
                });

        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(oldPolicy.getExpirationDate(), today);

        if (daysOverdue > 0) {
            if (oldPolicy.getType() != PolicyType.AUTO) {
                log.warn("Intento de renovación rechazado: póliza {} es {} (solo AUTO renovable post-vencimiento)",
                        oldPolicy.getPolicyNumber(), oldPolicy.getType());
                throw new IllegalArgumentException(PolicyConstants.ERROR_RENEW_NOT_AUTO);
            }
            if (daysOverdue > PolicyConstants.RENEWAL_WINDOW_DAYS) {
                log.warn("Intento de renovación rechazado: póliza {} vencida hace {} días (máx {} días)",
                        oldPolicy.getPolicyNumber(), daysOverdue, PolicyConstants.RENEWAL_WINDOW_DAYS);
                throw new IllegalArgumentException(
                        String.format(PolicyConstants.ERROR_RENEW_WINDOW_EXPIRED, PolicyConstants.RENEWAL_WINDOW_DAYS));
            }
        }

        oldPolicy.setStatus(PolicyStatus.RENOVADA);
        policyRepository.save(oldPolicy);

        Policy newPolicy = Policy.builder()
                .policyNumber(oldPolicy.getPolicyNumber() + "-R" + (oldPolicy.getRenewalCount() + 1))
                .type(oldPolicy.getType())
                .insurer(oldPolicy.getInsurer())
                .startDate(request.getNewExpirationDate().minusMonths(12))
                .expirationDate(request.getNewExpirationDate())
                .status(PolicyStatus.ACTIVO)
                .renewalCount(oldPolicy.getRenewalCount() + 1)
                .client(oldPolicy.getClient())
                .advisor(oldPolicy.getAdvisor())
                .build();

        newPolicy = policyRepository.save(newPolicy);

        log.info("Póliza {} renovada → {} (nuevo vencimiento: {})",
                oldPolicy.getPolicyNumber(), newPolicy.getPolicyNumber(), request.getNewExpirationDate());

        return toSummaryDTO(newPolicy);
    }

    @Override
    @Transactional
    public PolicySummaryDTO createPolicy(PolicyRequestDTO request) {
        log.debug("Creando póliza {} para cliente {}", request.getPolicyNumber(), request.getClientName());
        Advisor advisor = advisorRepository.findById(PolicyConstants.DEFAULT_ADVISOR_ID)
                .orElseThrow(() -> {
                    log.error("Asesor por defecto (ID={}) no encontrado", PolicyConstants.DEFAULT_ADVISOR_ID);
                    return new IllegalArgumentException(PolicyConstants.ADVISOR_NOT_FOUND);
                });

        Client client = self.findOrCreateClient(
                request.getClientName(),
                request.getClientPhone() != null ? request.getClientPhone() : "",
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

        log.info("Póliza {} creada para cliente {} (ID póliza: {}, ID cliente: {})",
                policy.getPolicyNumber(), client.getName(), policy.getId(), client.getId());

        return toSummaryDTO(policy);
    }

    @Override
    @Transactional
    public PolicySummaryDTO updatePolicy(PolicyRequestDTO request) {
        log.debug("Actualizando póliza ID {}", request.getId());
        Policy policy = policyRepository.findById(request.getId())
                .orElseThrow(() -> {
                    log.warn("Póliza no encontrada para actualización: {}", request.getId());
                    return new IllegalArgumentException(PolicyConstants.POLICY_NOT_FOUND + request.getId());
                });

        String oldNumber = policy.getPolicyNumber();
        String oldInsurer = policy.getInsurer();

        if (request.getPolicyNumber() != null) policy.setPolicyNumber(request.getPolicyNumber());
        if (request.getType() != null) policy.setType(PolicyType.valueOf(request.getType()));
        if (request.getInsurer() != null) policy.setInsurer(request.getInsurer());
        if (request.getStartDate() != null) policy.setStartDate(request.getStartDate());
        if (request.getExpirationDate() != null) policy.setExpirationDate(request.getExpirationDate());

        policy = policyRepository.save(policy);

        log.info("Póliza {} actualizada (aseguradora: {} → {})", policy.getPolicyNumber(), oldInsurer, policy.getInsurer());

        return toSummaryDTO(policy);
    }

    @Override
    @Transactional
    public ClientDetailDTO updateClient(Long clientId, ClientDetailDTO clientData) {
        log.debug("Actualizando cliente ID {}", clientId);
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado: {}", clientId);
                    return new IllegalArgumentException(PolicyConstants.CLIENT_NOT_FOUND + clientId);
                });

        String oldName = client.getName();
        String oldPhone = client.getPhone();

        if (clientData.getName() != null) client.setName(clientData.getName());
        if (clientData.getPhone() != null) client.setPhone(clientData.getPhone());
        if (clientData.getEmail() != null) client.setEmail(clientData.getEmail());
        if (clientData.getNotes() != null) client.setNotes(clientData.getNotes());

        client = clientRepository.save(client);

        log.info("Cliente {} actualizado (ID: {}, nombre: {} → {}, teléfono: {} → {})",
                oldName, client.getId(), oldName, client.getName(), oldPhone, client.getPhone());

        return self.getClientDetail(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDetailDTO getClientDetail(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException(PolicyConstants.CLIENT_NOT_FOUND + clientId));

        List<Policy> allPolicies = policyRepository.findByClientId(clientId);
        long activeCount = allPolicies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVO)
                .count();

        return ClientDetailDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .email(client.getEmail())
                .notes(client.getNotes())
                .activePolicies((int) activeCount)
                .totalPolicies(allPolicies.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDetailDTO> getAllClients(Long advisorId) {
        return clientRepository.findByAdvisorId(advisorId).stream()
                .map(c -> ClientDetailDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public Client findOrCreateClient(String name, String phone, String email, String notes, Long advisorId) {
        Optional<Client> existing = clientRepository.findByAdvisorIdAndNameAndPhone(advisorId, name, phone);
        if (existing.isPresent()) {
            Client client = existing.get();
            if (email != null && client.getEmail() == null) client.setEmail(email);
            if (notes != null && client.getNotes() == null) client.setNotes(notes);
            return client;
        }

        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException(PolicyConstants.ADVISOR_NOT_FOUND));

        Client newClient = Client.builder()
                .name(name)
                .phone(phone != null ? phone : "")
                .email(email)
                .notes(notes)
                .advisor(advisor)
                .build();
        return clientRepository.save(newClient);
    }

    private PolicySummaryDTO toSummaryDTO(Policy policy) {
        LocalDate today = LocalDate.now();
        Client client = policy.getClient();
        int attempts = contactAttemptRepository.countByPolicyId(policy.getId());

        String interestStatus = null;
        String lastContactResult = "No contactado";
        Optional<ContactAttempt> lastAttempt = contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(policy.getId());
        if (lastAttempt.isPresent()) {
            ContactAttemptResult r = lastAttempt.get().getResult();
            switch (r) {
                case CONTACTED -> lastContactResult = "Contactado";
                case NO_ANSWER -> lastContactResult = "No contestó";
                case LEFT_MESSAGE -> lastContactResult = "Dejó mensaje";
                case INTERESTED -> {
                    lastContactResult = "Interesado";
                    interestStatus = "Interesado";
                }
                case NOT_INTERESTED -> {
                    lastContactResult = "No interesado";
                    interestStatus = "No interesado";
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
            return PolicyConstants.PRIORITY_COMPLETADA;
        }
        if (policy.getStatus() == PolicyStatus.PERDIDO) {
            return PolicyConstants.PRIORITY_PERDIDO;
        }
        if (policy.getStatus() == PolicyStatus.VENCIDO) {
            return PolicyConstants.PRIORITY_URGENTE;
        }
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, policy.getExpirationDate());
        if (daysUntilExpiry <= 7) {
            return PolicyConstants.PRIORITY_ALTA;
        }
        if (daysUntilExpiry <= 30) {
            return PolicyConstants.PRIORITY_MEDIA;
        }
        return PolicyConstants.PRIORITY_BAJA;
    }

    private String calculateRecommendedAction(Policy policy, LocalDate today) {
        if (policy.getStatus() == PolicyStatus.RENOVADA || policy.getStatus() == PolicyStatus.PERDIDO) {
            return PolicyConstants.ACTION_ALREADY_MANAGED;
        }
        if (policy.getStatus() == PolicyStatus.VENCIDO) {
            long daysOverdue = ChronoUnit.DAYS.between(policy.getExpirationDate(), today);
            long remaining = PolicyConstants.RENEWAL_WINDOW_DAYS - daysOverdue;
            return String.format(PolicyConstants.ACTION_CONTACT_URGENT, PolicyConstants.RENEWAL_WINDOW_DAYS, remaining);
        }
        return PolicyConstants.ACTION_BEFORE_EXPIRY;
    }
}
