package com.strategyengine.strategyengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CandleDataToShowInGraphDTO {
    private List<CandlePoint> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CandlePoint {
        private long x;         // Timestamp in milliseconds
        private List<Double> y; // [open, high, low, close]
    }
}
