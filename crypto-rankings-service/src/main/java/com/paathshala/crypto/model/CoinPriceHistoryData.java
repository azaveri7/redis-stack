package com.paathshala.crypto.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CoinPriceHistoryData {
    private String change;
    private List<CoinPriceHistoryExchangeRate> history = new ArrayList<>();

    public List<CoinPriceHistoryExchangeRate> getHistory() {
        return history;
    }
}
