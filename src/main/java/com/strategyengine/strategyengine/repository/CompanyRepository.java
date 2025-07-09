package com.strategyengine.strategyengine.repository;

import com.strategyengine.strategyengine.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsBySymbol(String symbol);
    Optional<Company> findBySymbol(String symbol); // ✅ Enables fetching Company entity by symbol
}
