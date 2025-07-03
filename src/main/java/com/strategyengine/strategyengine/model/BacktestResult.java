package com.strategyengine.strategyengine.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BacktestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double initialEquity;
    private double finalEquity;
    private int totalTrades;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Trade> trades;

    @ManyToOne
    private Strategy strategy;
}
