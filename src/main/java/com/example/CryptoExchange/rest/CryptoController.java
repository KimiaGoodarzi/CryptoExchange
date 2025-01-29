package com.example.CryptoExchange.rest;

import com.example.CryptoExchange.Price;
import com.example.CryptoExchange.PriceRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow React frontend
public class CryptoController {

    private final PriceRepository priceRepository;

    public CryptoController(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @GetMapping("/prices")
    public List<Price> getPrices() {
        // Fetch all prices from the database
        return priceRepository.findAll();
    }
}
