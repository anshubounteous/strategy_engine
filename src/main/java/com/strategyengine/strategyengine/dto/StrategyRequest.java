package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyRequest {
    private String strategyName;       // Optional: predefined strategy
    private String strategyScript;     // Optional: DSL-based script
    private String symbol;
    private LocalDate startDate;
    private LocalDate endDate;
    private double initialCapital;     // Optional, default = 10000
}
