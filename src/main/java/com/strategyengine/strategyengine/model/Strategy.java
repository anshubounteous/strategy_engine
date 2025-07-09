package com.strategyengine.strategyengine.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> symbolList;            // Stock symbol like "TCS"
    private String script;            // For user-defined DSL
    private String paramsJson;
    private String symbol;// JSON string for predefined param (optional)

    private LocalDate startDate;
    private LocalDate endDate;
}
