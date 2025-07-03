package com.strategyengine.strategyengine.model;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String action; // "BUY" or "SELL"
    private double price;

    @ManyToOne
    private Strategy strategy;
}
