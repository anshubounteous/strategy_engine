package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDTO {
    private LocalDate date;
    private String action; // BUY or SELL
    private double price;

    // NEW FIELDS for trade table
    private String symbol;
    private int quantity;
    private double totalCostPrice;
    private double openingBalance;
    private double closingBalance;
    private double nav;
    private double realizedProfit;
}
