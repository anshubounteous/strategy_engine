package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDTO {
    private LocalDate date;
    private String action;   // BUY or SELL
    private double price;
}
