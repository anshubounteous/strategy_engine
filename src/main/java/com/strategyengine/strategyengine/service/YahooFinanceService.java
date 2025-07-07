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
                    "UPL.NS"             // 🔁 added
            );
            case "NIFTY NEXT 50" -> List.of(
                    "BOSCHLTD.NS",
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
                    "JINDALSTEL.NS"
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
