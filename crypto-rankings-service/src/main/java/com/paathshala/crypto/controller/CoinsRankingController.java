package com.paathshala.crypto.controller;

import com.paathshala.crypto.model.CoinInfo;
import com.paathshala.crypto.model.HistoryData;
import com.paathshala.crypto.service.CoinsDataService;
import com.paathshala.crypto.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.dengliming.redismodule.redistimeseries.Sample;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/coins")
@Slf4j
public class CoinsRankingController {

    @Autowired
    private CoinsDataService coinsDataService;

    @GetMapping
    public ResponseEntity<List<CoinInfo>> fetchAllCoins() {
        return ResponseEntity.ok()
                .body(coinsDataService.fetchAllCoinsFromRedisJSON());
    }

    @GetMapping("/{symbol}/{timePeriod}")
    public List<HistoryData> fetchCoinHistoryForTimePeriod(
            @PathVariable String symbol,
            @PathVariable String timePeriod
    ) {
        List<Sample.Value>  coinTSData = coinsDataService
                .fetchCoinHistoryPerTimePeriodFromRedisTS(symbol, timePeriod);
        List<HistoryData> coinHistory =
                coinTSData.stream().map(value ->
                    new HistoryData(
                            Utility.convertUnixTimeToDate(value.getTimestamp()),
                            Utility.round(value.getValue(), 2)
                    )
                ).collect(Collectors.toList());
        return coinHistory;
    }

}
