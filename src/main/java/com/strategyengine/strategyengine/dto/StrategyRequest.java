package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyRequest {
    private String strategyName;       // Optional: predefined strategy
    private String strategyScript;     // Optional: DSL-based script
    private List<String> symbolList;
    private LocalDate startDate;
    private LocalDate endDate;
    private double initialCapital;
    private String symbol;// Optional, default = 10000
}
