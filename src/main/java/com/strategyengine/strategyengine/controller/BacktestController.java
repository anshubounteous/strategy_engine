package com.strategyengine.strategyengine.controller;


import com.strategyengine.strategyengine.dto.BacktestResultDTO;
import com.strategyengine.strategyengine.dto.CandleDataToShowInGraphDTO;
import com.strategyengine.strategyengine.dto.StrategyRequest;
import com.strategyengine.strategyengine.dto.TradeDTO;
import com.strategyengine.strategyengine.engine.StrategyEngine;
import com.strategyengine.strategyengine.model.BacktestResult;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.model.Strategy;
import com.strategyengine.strategyengine.repository.CandleRepository;
import com.strategyengine.strategyengine.service.CandleGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final CandleRepository candleRepository;
    private final StrategyEngine strategyEngine;
    private final CandleGraphService candleGraphService;

    @PostMapping
    public BacktestResultDTO runBacktest(@RequestBody StrategyRequest request) {

        List<String> symbolListToBeUse=new ArrayList<>();
        List<String>nifty50=List.of(
                "HDFCBANK.NS",   // ~13.2%
                "ICICIBANK.NS",  // ~9.1%
                "RELIANCE.NS",   // ~8.6%
                "TCS.NS",        // ~5.0%
                "BHARTIARTL.NS", // ~4.4%
                "INFY.NS",
                "BAJFINANCE.NS",
                "HINDUNILVR.NS",
                "ITC.NS",
                "LT.NS",
                "HCLTECH.NS",
                "KOTAKBANK.NS",
                "ULTRACEMCO.NS",
                "AXISBANK.NS",
                "TITAN.NS",
                "NTPC.NS",
                "ASIANPAINT.NS",
                "NESTLEIND.NS",
                "SBIN.NS",
                "SUNPHARMA.NS",
                "MARUTI.NS",
                "M&M.NS",
                "JSWSTEEL.NS",
                "TATAMOTORS.NS",
                "TATASTEEL.NS",
                "TECHM.NS",
                "WIPRO.NS",
                "ADANIENT.NS",
                "ADANIPORTS.NS",
                "COALINDIA.NS",
                "POWERGRID.NS",
                "DRREDDY.NS",
                "CIPLA.NS",
                "EICHERMOT.NS",
                "HEROMOTOCO.NS",
                "HINDALCO.NS",
                "INDUSINDBK.NS",
                "SHREECEM.NS",
                "BPCL.NS",
                "ONGC.NS",
                "GRASIM.NS",
                "IOC.NS",
                "HDFCLIFE.NS",
                "SBILIFE.NS",
                "BAJAJ-AUTO.NS",
                "BAJAJFINSV.NS",     // 🔁 added
                "BRITANNIA.NS",      // 🔁 added
                "HDFC.NS",           // 🔁 added
                "UPL.NS");

        List<String>niftyNext50=List.of( "BOSCHLTD.NS",
                "ABB.NS",
                "APOLLOHOSP.NS",
                "AMBUJACEM.NS",
                "ADANIGREEN.NS",
                "BIOCON.NS",
                "BEL.NS",
                "BANKBARODA.NS",
                "BANDHANBNK.NS",
                "CANBK.NS",
                "CHOLAFIN.NS",
                "COLPAL.NS",
                "DABUR.NS",
                "DLF.NS",
                "GODREJCP.NS",
                "GAIL.NS",
                "HAVELLS.NS",
                "ICICIPRULI.NS",
                "INDIGO.NS",
                "LTIM.NS",
                "LTTS.NS",
                "L&TFH.NS",
                "LICI.NS",
                "MCDOWELL-N.NS",
                "MFSL.NS",
                "MUTHOOTFIN.NS",
                "NAUKRI.NS",
                "NHPC.NS",
                "NMDC.NS",
                "OFSS.NS",
                "PAGEIND.NS",
                "PETRONET.NS",
                "PIDILITIND.NS",
                "PIIND.NS",
                "PFC.NS",
                "RECLTD.NS",
                "SAIL.NS",
                "SIEMENS.NS",
                "SRF.NS",
                "TORNTPHARM.NS",
                "TRENT.NS",
                "TVSMOTOR.NS",
                "UBL.NS",
                "VOLTAS.NS",
                "ZYDUSLIFE.NS",
                "AUROPHARMA.NS",
                "ALKEM.NS",
                "INDUSTOWER.NS",
                "IOCL.NS",
                "JINDALSTEL.NS");

        List<String>symbolL=request.getSymbolList();
        for(String k : symbolL){
            if(k.equals("NIFTY 50")) {
                symbolListToBeUse.addAll(nifty50);
            }else if(k.equals("NIFTY NEXT 50")){
                symbolListToBeUse.addAll(niftyNext50);
            }else if(k.equals("NIFTY NEXT 50")){
                symbolListToBeUse.addAll(nifty50);
                symbolListToBeUse.addAll(niftyNext50);
            }else{
                symbolListToBeUse.add(k);
            }
        }
        HashMap<String,List<Candle>> CandleMap=new HashMap<>();
        for(String s:symbolListToBeUse){
            List<Candle> candles = candleRepository
                    .findBySymbolAndDateBetweenOrderByDateAsc(
                            s, request.getStartDate(), request.getEndDate());
            CandleMap.put(s,candles);
        }
        Strategy strategy = Strategy.builder()
                .name(request.getStrategyName())
                .symbolList(symbolListToBeUse)
                .script(request.getStrategyScript())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        BacktestResult result = strategyEngine.run(strategy, CandleMap);

        return BacktestResultDTO.builder()
                .initialEquity(result.getInitialEquity())
                .finalEquity(result.getFinalEquity())
                .totalTrades(result.getTotalTrades())
                .trades(result.getTrades().stream()
                        .map(t -> TradeDTO.builder()
                                .date(String.valueOf(t.getDate()))
                                .price(t.getPrice())
                                .action(t.getAction())
                                .symbol(t.getSymbol())
                                .quantity(t.getQuantity())
                                .totalCostPrice(t.getTotalCostPrice())
                                .openingBalance(t.getOpeningBalance())
                                .closingBalance(t.getClosingBalance())
                                .nav(t.getNav())
                                .realizedProfit(t.getRealizedProfit())
                                .build())
                        .collect(Collectors.toList()))

                .build();
    }

    @PostMapping ("/candles")
    public Map<String, List<CandleDataToShowInGraphDTO.CandlePoint>> candleData(@RequestBody StrategyRequest request) {
        return candleGraphService.getCandleDataForChart(request);
    }

}

