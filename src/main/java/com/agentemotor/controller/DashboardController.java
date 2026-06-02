package com.agentemotor.controller;

import com.agentemotor.dto.DashboardStatsDTO;
import com.agentemotor.dto.PolicyDetailDTO;
import com.agentemotor.dto.PolicySummaryDTO;
import com.agentemotor.service.PolicyService;
import com.agentemotor.utils.PolicyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PolicyService policyService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(defaultValue = "all") String filter,
            Model model) {

        DashboardStatsDTO stats = policyService.getDashboardStats(PolicyConstants.DEFAULT_ADVISOR_ID);
        List<PolicySummaryDTO> policies = policyService.getPolicies(PolicyConstants.DEFAULT_ADVISOR_ID, filter);

        model.addAttribute("stats", stats);
        model.addAttribute("policies", policies);
        model.addAttribute("currentFilter", filter);
        return "dashboard";
    }

    @GetMapping("/policy/{id}")
    @ResponseBody
    public PolicyDetailDTO policyDetail(@PathVariable Long id) {
        return policyService.getPolicyDetail(id);
    }

    @GetMapping("/policy/nueva")
    public String newPolicyForm() {
        return "policy-form";
    }
}
