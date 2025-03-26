package com.example.CryptoExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CoinbaseService {

    private PriceRepository priceRepository;
    private ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final List<String> symbols = List.of("BTC-USDT", "ETH-USDT");

    public CoinbaseService(PriceRepository priceRepository, SimpMessagingTemplate messagingTemplate) {

        this.priceRepository = priceRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();

    }

    public void fetchPrices() {

        RestTemplate restTemplate = new RestTemplate();
        for (String symbol : symbols) {

            try {
                String url = String.format("https://api.coinbase.com/v2/prices/%s/spot", symbol);
                String response = restTemplate.getForObject(url, String.class);
                JsonNode jsonNode = objectMapper.readTree(response);
                double price = jsonNode.get("data").get("amount").asDouble();

                Optional<Price> existingPrice = priceRepository.findBySymbol(symbol);
                Price priceRecord;

                if (existingPrice.isPresent()) {

                    priceRecord = existingPrice.get();
                    double prevPrice = priceRecord.getPrice();
                    double change = Math.abs((price - prevPrice) / prevPrice) * 100;

                    if (change > 0.1) {
                        priceRecord.setPrice(price);
                        priceRecord.setTimestamp(LocalDateTime.now());
                        priceRepository.save(priceRecord);

                        System.out.printf("Coinbase updated %s: $%.2f (%.2f%% change)%n", symbol, price, change);

                        messagingTemplate.convertAndSend("/topic/prices" , Map.of(
                                "exchange", "Coinbase",
                                "symbol" , priceRecord.getSymbol(),
                                "price" , priceRecord.getPrice(),
                                "timestamp", priceRecord.getTimestamp().toString()

                        ));
                    }


                } else {
                    priceRecord = new Price();
                    priceRecord.setExchange("Coinbase");
                    priceRecord.setSymbol(symbol);
                    priceRecord.setPrice(price);
                    priceRecord.setTimestamp(LocalDateTime.now());
                    priceRepository.save(priceRecord);

                    System.out.printf("Coinbase saved new %s: $%.2f", symbol, price);

                    //we convert the Java Map to json and send it to the websocket destination (topic/prices)
                    messagingTemplate.convertAndSend("/topic/prices" , Map.of(

                            "exchange", "Coinbase",
                            "symbol", priceRecord.getSymbol(),
                            "price", priceRecord.getPrice(),
                            "timestamp", priceRecord.getTimestamp().toString()

                    ));
                }

            }
                catch (JsonProcessingException e) {
                    System.err.println("Error parsing Coinbase JSON: " + e.getMessage());
            } catch (Exception e) {

                System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
            }


        }

    }

}