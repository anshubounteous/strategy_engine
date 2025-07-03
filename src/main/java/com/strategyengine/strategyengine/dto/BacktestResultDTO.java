package com.strategyengine.strategyengine.dto;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BacktestResultDTO {
    private double initialEquity;
    private double finalEquity;
    private int totalTrades;
    private List<TradeDTO> trades;
}
