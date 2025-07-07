package com.strategyengine.strategyengine.service;

import com.strategyengine.strategyengine.dto.CandleDataToShowInGraphDTO;
import com.strategyengine.strategyengine.dto.StrategyRequest;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandleGraphService {

    private final CandleRepository candleRepository;

    public CandleDataToShowInGraphDTO getCandleDataForChart(StrategyRequest request) {
        List<Candle> candles = candleRepository.findBySymbolAndDateBetweenOrderByDateAsc(
                request.getSymbol(), request.getStartDate(), request.getEndDate());

        List<CandleDataToShowInGraphDTO.CandlePoint> points = candles.stream().map(candle ->
                CandleDataToShowInGraphDTO.CandlePoint.builder()
                        .x(candle.getDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                        .y(List.of(candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose()))
                        .build()
        ).collect(Collectors.toList());

        return CandleDataToShowInGraphDTO.builder()
                .data(points)
                .build();
    }
}
