package com.strategyengine.strategyengine.model;

import java.util.List;

public record ImportResult(List<Candle> candles, List<String> failedSymbols) {}
