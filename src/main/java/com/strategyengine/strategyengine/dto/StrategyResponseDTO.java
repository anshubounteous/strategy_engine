package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyResponseDTO {
    private Long id;
    private String name;
    private String symbol;
    private String script;         // DSL or empty for predefined
    private String paramsJson;     // Optional config for predefined
    private LocalDate startDate;
    private LocalDate endDate;
}
