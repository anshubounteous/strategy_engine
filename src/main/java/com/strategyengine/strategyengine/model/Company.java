package com.strategyengine.strategyengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;       // e.g. RELIANCE.NS
    private String name;         // e.g. Reliance Industries
    private String indexName;    // e.g. NIFTY 50

    @Column(precision = 30, scale = 4)
    private BigDecimal totalCapital; // Market Cap

    private Double riskFactor;   // Beta * Market Risk Premium
}


