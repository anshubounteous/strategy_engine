package com.strategyengine.strategyengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategyengine.strategyengine.model.Candle;
import com.strategyengine.strategyengine.util.YahooFinanceParser;
import com.strategyengine.strategyengine.model.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YahooFinanceService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer
            .build();

    public List<String> getSymbolsForIndex(String indexName) {
        return switch (indexName) {
            case "NIFTY 50" -> List.of(
                    "ADANIENT.NS", "ADANIPORTS.NS", "ASIANPAINT.NS", "AXISBANK.NS",
                    "BAJAJ-AUTO.NS", "BAJAJFINSV.NS", "BAJAJFINANCE.NS", "BHARTIARTL.NS",
                    "BPCL.NS", "BRITANNIA.NS", "CIPLA.NS", "COALINDIA.NS", "DRREDDY.NS",
                    "EICHERMOT.NS", "GRASIM.NS", "HCLTECH.NS", "HDFCBANK.NS", "HDFC.NS",
                    "HDFCLIFE.NS", "HEROMOTOCO.NS", "HINDALCO.NS", "HINDUNILVR.NS",
                    "ICICIBANK.NS", "INDUSINDBK.NS", "INFY.NS", "IOC.NS", "ITC.NS",
                    "JSWSTEEL.NS", "KOTAKBANK.NS", "LT.NS", "M&M.NS", "MARUTI.NS",
                    "NESTLEIND.NS", "NTPC.NS", "ONGC.NS", "POWERGRID.NS", "RELIANCE.NS",
                    "SBILIFE.NS", "SBIN.NS", "SHREECEM.NS", "SUNPHARMA.NS", "TATACONSUM.NS",
                    "TATAMOTORS.NS", "TATASTEEL.NS", "TCS.NS", "TECHM.NS", "TITAN.NS",
                    "ULTRACEMCO.NS", "UPL.NS", "WIPRO.NS"
            );
            case "NIFTY NEXT 50" -> List.of(
                    "ABB.NS", "ADANIGREEN.NS", "ALKEM.NS", "AMBUJACEM.NS", "APOLLOHOSP.NS",
                    "AUROPHARMA.NS", "BANDHANBNK.NS", "BANKBARODA.NS", "BEL.NS", "BIOCON.NS",
                    "BOSCHLTD.NS", "CANBK.NS", "CHOLAFIN.NS", "COLPAL.NS", "DABUR.NS",
                    "DLF.NS", "GAIL.NS", "GODREJCP.NS", "HAVELLS.NS", "ICICIPRULI.NS",
                    "IGL.NS", "INDIGO.NS", "INDUSTOWER.NS", "IOC.NS", "JINDALSTEL.NS",
                    "L&TFH.NS", "LICI.NS", "LTIM.NS", "LTTS.NS", "MCDOWELL-N.NS",
                    "MFSL.NS", "MUTHOOTFIN.NS", "NAUKRI.NS", "NHPC.NS", "NMDC.NS",
                    "OFSS.NS", "PAGEIND.NS", "PETRONET.NS", "PIDILITIND.NS", "PIIND.NS",
                    "PFC.NS", "RECLTD.NS", "SAIL.NS", "SIEMENS.NS", "SRF.NS", "TORNTPHARM.NS",
                    "TRENT.NS", "TVSMOTOR.NS", "UBL.NS", "VOLTAS.NS", "ZYDUSLIFE.NS"
            );
            case "NIFTY 100" -> {
                List<String> combined = new ArrayList<>();
                combined.addAll(getSymbolsForIndex("NIFTY 50"));
                combined.addAll(getSymbolsForIndex("NIFTY NEXT 50"));
                yield combined;
            }
            default -> throw new IllegalArgumentException("Unknown index: " + indexName);
        };
    }

    public List<Candle> fetchHistoricalData(String symbol) {
        long period1 = 946684800L; // Jan 1, 2000
        long period2 = System.currentTimeMillis() / 1000;

        String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d",
                symbol, period1, period2
        );

        try {
            String rawJson = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        System.err.println("❌ Error fetching for symbol: " + symbol + " → " + e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (rawJson == null || rawJson.isBlank()) {
                System.err.println("❌ Empty or null response for symbol: " + symbol);
                return List.of(); // Treat as failed
            }

            JsonNode root = objectMapper.readTree(rawJson);
            return YahooFinanceParser.parse(root, symbol);

        } catch (Exception e) {
            System.err.println("❌ Exception for " + symbol + ": " + e.getMessage());
            return List.of(); // fail safely
        }
    }

    public ImportResult importIndex(String indexName) {
        List<String> symbols = getSymbolsForIndex(indexName);
        List<Candle> allCandles = new ArrayList<>();
        List<String> failedSymbols = new ArrayList<>();

        for (String symbol : symbols) {
            List<Candle> candles = fetchHistoricalData(symbol);
            if (candles.isEmpty()) {
                failedSymbols.add(symbol);
            } else {
                allCandles.addAll(candles);
            }
        }

        return new ImportResult(allCandles, failedSymbols);
    }
}
