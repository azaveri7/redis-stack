package com.paathshala.crypto.model;

import lombok.Data;

@Data
public class CoinPriceHistoryExchangeRate {
    private String price;
    private String timestamp;

    public String getPrice(){
        return price;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
