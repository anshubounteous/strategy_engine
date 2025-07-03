package com.strategyengine.strategyengine.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;              // e.g. "RSI-Based Strategy"
    private String symbol;            // Stock symbol like "TCS"
    private String script;            // For user-defined DSL
    private String paramsJson;        // JSON string for predefined param (optional)

    private LocalDate startDate;
    private LocalDate endDate;
}
