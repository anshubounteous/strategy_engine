package com.strategyengine.strategyengine.repository;

import com.strategyengine.strategyengine.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsBySymbol(String symbol);
}
