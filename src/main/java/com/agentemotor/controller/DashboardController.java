package com.agentemotor.controller;

import com.agentemotor.dto.PolicyDetailDTO;
import com.agentemotor.dto.PolicySummaryDTO;
import com.agentemotor.service.PolicyService;
import com.agentemotor.service.StatsService;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final PolicyService policyService;
    private final StatsService statsService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(defaultValue = AppConstants.FILTER_ALL) String filter,
            Model model) {

        var stats = statsService.getDashboardStats(AppConstants.DEFAULT_ADVISOR_ID);
        List<PolicySummaryDTO> policies = policyService.getPolicies(AppConstants.DEFAULT_ADVISOR_ID, filter);

        model.addAttribute("stats", stats);
        model.addAttribute("policies", policies);
        model.addAttribute("currentFilter", filter);
        return AppConstants.VIEW_DASHBOARD;
    }

    @GetMapping("/policy/{id}")
    @ResponseBody
    public PolicyDetailDTO policyDetail(@PathVariable Long id) {
        return policyService.getPolicyDetail(id);
    }

    @GetMapping("/policy/nueva")
    public String newPolicyForm() {
        return AppConstants.VIEW_POLICY_FORM;
    }
}
