package com.paathshala.crypto.model;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class CoinData {
    private CoinStats stats;
    private List<CoinInfo> coins = new ArrayList<>();

    public List<CoinInfo> getCoins() {
        return coins;
    }
}
