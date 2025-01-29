package com.example.CryptoExchange;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class BitpinPollingService {
    private static final String BITPIN_API_URL = "https://api.bitpin.ir/api/v1/mth/orderbook/{symbol}/";

    private final PriceRepository priceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BitpinPollingService(PriceRepository priceRepository, SimpMessagingTemplate messagingTemplate) {
        this.priceRepository = priceRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 3000)
    public void fetchBitpinPrices() {
        fetchAndBroadcastPrice("BTC_USDT");
        fetchAndBroadcastPrice("ETH_USDT");
    }

    private void fetchAndBroadcastPrice(String symbol) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = BITPIN_API_URL.replace("{symbol}", symbol);

            BitpinResponse response = restTemplate.getForObject(url, BitpinResponse.class);
            if (response != null && !response.getBids().isEmpty() && !response.getAsks().isEmpty()) {
                Double buyPrice = Double.parseDouble(response.getBids().get(0).get(0));
                Double sellPrice = Double.parseDouble(response.getAsks().get(0).get(0));
                Double averagePrice = (buyPrice + sellPrice) / 2;

                String normalizedSymbol = symbol.replace("_", " ");
                Optional<Price> existingPrice = priceRepository.findBySymbol(normalizedSymbol);
                Price priceRecord;

                if (existingPrice.isPresent()) {
                    priceRecord = existingPrice.get();
                    priceRecord.setPrice(averagePrice);
                    priceRecord.setTimestamp(LocalDateTime.now());
                    priceRepository.save(priceRecord);
                } else {
                    priceRecord = new Price();
                    priceRecord.setExchange("Bitpin");
                    priceRecord.setSymbol(normalizedSymbol);
                    priceRecord.setPrice(averagePrice);
                    priceRecord.setTimestamp(LocalDateTime.now());
                    priceRepository.save(priceRecord);
                }

                // Broadcast the update
                messagingTemplate.convertAndSend("/topic/prices", Map.of(
                        "exchange", "Bitpin",
                        "symbol", priceRecord.getSymbol(),
                        "price", priceRecord.getPrice(),
                        "timestamp", priceRecord.getTimestamp().toString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
