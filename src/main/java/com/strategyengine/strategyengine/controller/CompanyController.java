package com.strategyengine.strategyengine.controller;

import com.strategyengine.strategyengine.model.Company;
import com.strategyengine.strategyengine.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/update-risk-data")
    public String updateRiskData() {
        companyService.updateCompanyDetailsWithRiskData();
        return "Company risk data and total capital updated.";
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }
}
