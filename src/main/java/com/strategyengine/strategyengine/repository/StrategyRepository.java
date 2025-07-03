package com.strategyengine.strategyengine.repository;


import com.strategyengine.strategyengine.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    // Spring Data JPA gives you: save, findById, findAll, delete, etc.
}
