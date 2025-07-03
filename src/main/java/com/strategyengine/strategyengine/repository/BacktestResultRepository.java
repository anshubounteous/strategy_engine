package com.strategyengine.strategyengine.repository;


import com.strategyengine.strategyengine.model.BacktestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {
    // Optional — use only if you want to persist past results
}
