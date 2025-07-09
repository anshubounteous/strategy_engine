package com.strategyengine.strategyengine.service;

import com.strategyengine.strategyengine.dto.CandleDataToShowInGraphDTO;
import com.strategyengine.strategyengine.dto.StrategyRequest;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandleGraphService {

    private final CandleRepository candleRepository;

    public Map<String, List<CandleDataToShowInGraphDTO.CandlePoint>> getCandleDataForChart(StrategyRequest request) {

        List<String> symbolListToBeUse = new ArrayList<>();
        List<String> nifty50 = List.of(
                "HDFCBANK.NS", "ICICIBANK.NS", "RELIANCE.NS", "TCS.NS", "BHARTIARTL.NS",
                "INFY.NS", "BAJFINANCE.NS", "HINDUNILVR.NS", "ITC.NS", "LT.NS",
                "HCLTECH.NS", "KOTAKBANK.NS", "ULTRACEMCO.NS", "AXISBANK.NS", "TITAN.NS",
                "NTPC.NS", "ASIANPAINT.NS", "NESTLEIND.NS", "SBIN.NS", "SUNPHARMA.NS",
                "MARUTI.NS", "M&M.NS", "JSWSTEEL.NS", "TATAMOTORS.NS", "TATASTEEL.NS",
                "TECHM.NS", "WIPRO.NS", "ADANIENT.NS", "ADANIPORTS.NS", "COALINDIA.NS",
                "POWERGRID.NS", "DRREDDY.NS", "CIPLA.NS", "EICHERMOT.NS", "HEROMOTOCO.NS",
                "HINDALCO.NS", "INDUSINDBK.NS", "SHREECEM.NS", "BPCL.NS", "ONGC.NS",
                "GRASIM.NS", "IOC.NS", "HDFCLIFE.NS", "SBILIFE.NS", "BAJAJ-AUTO.NS",
                "BAJAJFINSV.NS", "BRITANNIA.NS", "HDFC.NS", "UPL.NS");

        List<String> niftyNext50 = List.of(
                "BOSCHLTD.NS", "ABB.NS", "APOLLOHOSP.NS", "AMBUJACEM.NS", "ADANIGREEN.NS",
                "BIOCON.NS", "BEL.NS", "BANKBARODA.NS", "BANDHANBNK.NS", "CANBK.NS",
                "CHOLAFIN.NS", "COLPAL.NS", "DABUR.NS", "DLF.NS", "GODREJCP.NS",
                "GAIL.NS", "HAVELLS.NS", "ICICIPRULI.NS", "INDIGO.NS", "LTIM.NS",
                "LTTS.NS", "L&TFH.NS", "LICI.NS", "MCDOWELL-N.NS", "MFSL.NS",
                "MUTHOOTFIN.NS", "NAUKRI.NS", "NHPC.NS", "NMDC.NS", "OFSS.NS",
                "PAGEIND.NS", "PETRONET.NS", "PIDILITIND.NS", "PIIND.NS", "PFC.NS",
                "RECLTD.NS", "SAIL.NS", "SIEMENS.NS", "SRF.NS", "TORNTPHARM.NS",
                "TRENT.NS", "TVSMOTOR.NS", "UBL.NS", "VOLTAS.NS", "ZYDUSLIFE.NS",
                "AUROPHARMA.NS", "ALKEM.NS", "INDUSTOWER.NS", "IOCL.NS", "JINDALSTEL.NS");

        for (String k : request.getSymbolList()) {
            if (k.equals("NIFTY 50")) {
                symbolListToBeUse.addAll(nifty50);
            } else if (k.equals("NIFTY NEXT 50")) {
                symbolListToBeUse.addAll(niftyNext50);
            } else {
                symbolListToBeUse.add(k);
            }
        }

        Map<String, List<CandleDataToShowInGraphDTO.CandlePoint>> graphDataMap = new HashMap<>();

        for (String symbol : symbolListToBeUse) {
            List<Candle> candles = candleRepository.findBySymbolAndDateBetweenOrderByDateAsc(
                    symbol, request.getStartDate(), request.getEndDate());

            List<CandleDataToShowInGraphDTO.CandlePoint> points = candles.stream().map(candle ->
                    CandleDataToShowInGraphDTO.CandlePoint.builder()
                            .x(candle.getDate().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
                            .y(List.of(candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose()))
                            .build()
            ).collect(Collectors.toList());

            graphDataMap.put(symbol, points);
        }

        return graphDataMap;
    }
}