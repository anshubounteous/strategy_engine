package com.strategyengine.strategyengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strategyengine.strategyengine.model.Company;
import com.strategyengine.strategyengine.repository.CompanyRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CompanyService {

    private static final String API_KEY = "IJOV16DSYPG7J2CS"; //
    private static final double MARKET_RISK_PREMIUM = 0.06;

    @Autowired
    private CompanyRepository companyRepository;

    public void updateCompanyDetailsWithRiskData() {
        ObjectMapper mapper = new ObjectMapper();
        List<Company> companies = companyRepository.findAll();

        for (Company company : companies) {
            String symbol = convertToAlphaSymbol(company.getSymbol()); // Fix: use .NS and remove dashes
            String url = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + symbol + "&apikey=" + API_KEY;

            try {
                String json = Jsoup.connect(url)
                        .ignoreContentType(true)
                        .timeout(10000)
                        .userAgent("Mozilla/5.0")
                        .execute()
                        .body();

                JsonNode node = mapper.readTree(json);

                if (node == null || node.isEmpty() || node.has("Note")) {
                    System.err.println("⛔ API limit or empty response for " + symbol);
                    Thread.sleep(15000); // pause to respect rate limits
                    continue;
                }

                // Market Cap
                String marketCapStr = node.path("MarketCapitalization").asText();
                BigDecimal marketCap = marketCapStr.isEmpty() ? null : new BigDecimal(marketCapStr);

                // Beta
                String betaStr = node.path("Beta").asText();
                Double beta = betaStr.isEmpty() ? null : Double.parseDouble(betaStr);

                // Risk Factor
                Double riskFactor = (beta != null) ? beta * MARKET_RISK_PREMIUM : null;

                // Save data
                company.setTotalCapital(marketCap);
                company.setRiskFactor(riskFactor);
                companyRepository.save(company);

                System.out.println("✅ " + symbol + " | MarketCap: " + marketCap + " | Beta: " + beta);
                Thread.sleep(15000); // throttle to 4 requests/min

            } catch (Exception e) {
                System.err.println("❌ Failed for symbol: " + symbol + " | Reason: " + e.getMessage());
            }
        }
    }

    // ✅ Only clean the symbol if needed — keep ".NS", just remove dashes
    private String convertToAlphaSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }
}
