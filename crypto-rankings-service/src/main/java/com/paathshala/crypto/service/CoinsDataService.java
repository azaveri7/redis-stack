package com.paathshala.crypto.service;

import com.paathshala.crypto.model.*;
import com.paathshala.crypto.utils.HttpUtils;
import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import io.github.dengliming.redismodule.redistimeseries.DuplicatePolicy;
import io.github.dengliming.redismodule.redistimeseries.RedisTimeSeries;
import io.github.dengliming.redismodule.redistimeseries.Sample;
import io.github.dengliming.redismodule.redistimeseries.TimeSeriesOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CoinsDataService {

    public static final String GET_COINS_API = "https://coinranking1.p.rapidapi.com/coins?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h&tiers%5B0%5D=1&orderBy=marketCap&orderDirection=desc&limit=50&offset=0";
    public static final String GET_COIN_HISTORY = "https://coinranking1.p.rapidapi.com/coin/";
    public static final String COIN_HISTORY_TIME_PERIOD_PARAM = "/history?timePeriod=";
    public static final List<String> timePeriods = List.of("24h", "7d", "30d", "3m", "1y", "3y", "5y");
    private static final Logger log = LoggerFactory.getLogger(CoinsDataService.class);
    public static final String REDIS_KEY_COINS = "coins";


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisJSON redisJSON;

    @Autowired
    private RedisTimeSeries redisTimeSeries;

    public void fetchCoins() {
        log.info("Inside fetchCoins()");
        ResponseEntity<Coins> responseEntity = restTemplate.exchange(GET_COINS_API,
                HttpMethod.GET,
                HttpUtils.getHttpEntity(),
                Coins.class);
        storeCoinsToRedisJSON(responseEntity.getBody());
    }

    public void fetchCoinHistory() {
        log.info("Inside fetchCoinHistory()");
        List<CoinInfo> allCoins = getAllCoinsFromRedisJSON();
        allCoins.forEach(coinInfo -> {
            timePeriods.forEach(s -> {
                fetchCoinHistoryForTimePeriod(coinInfo, s);
            });
        });
    }

    private void fetchCoinHistoryForTimePeriod(CoinInfo coinInfo, String timePeriod) {
        log.info("Inside fetchCoinHistoryForTimePeriod() to fetch coin history of {} for time period {}",
                coinInfo.getName(), timePeriod);
        String url = GET_COIN_HISTORY
                + coinInfo.getUuid()
                + COIN_HISTORY_TIME_PERIOD_PARAM
                + timePeriod;
        //log.info("URL for coin history is {}", url);
        ResponseEntity<CoinPriceHistory> coinPriceHistoryResponseEntity = restTemplate.exchange(url,
                HttpMethod.GET,
                HttpUtils.getHttpEntity(),
                CoinPriceHistory.class);
        log.info("Successfully fetched data for coin history of {} for time period {}",
                coinInfo.getName(), timePeriod);
        storeCoinHistoryToRedisTS(coinPriceHistoryResponseEntity.getBody(),
                coinInfo.getSymbol(),
                timePeriod);
    }

    private void storeCoinHistoryToRedisTS(CoinPriceHistory coinPriceHistory,
                                           String symbol, String timePeriod) {
        log.info("Storing Coin History of {} for time period {}",
                symbol, timePeriod);
        List<CoinPriceHistoryExchangeRate> coinPriceHistoryExchangeRateList =
                coinPriceHistory.getData().getHistory();
        //Symbol:timePeriod
        //BTC:24h, BTC:1y, ETH:3y
        coinPriceHistoryExchangeRateList.stream()
                .filter(ch ->
                        Objects.nonNull(ch.getPrice()) && Objects.nonNull(ch.getTimestamp()))
                .forEach(ch -> {
                    redisTimeSeries.add(new Sample(symbol + ":" + timePeriod,
                            Sample.Value.of(Long.valueOf(ch.getTimestamp()), Double.valueOf(ch.getPrice()))),
                            new TimeSeriesOptions()
                                    .unCompressed()
                                    .duplicatePolicy(DuplicatePolicy.LAST));
                });
        log.info("Completed storing Coin History of {} for time period {}",
                symbol, timePeriod);
    }

    private List<CoinInfo> getAllCoinsFromRedisJSON() {
        CoinData coinData = redisJSON.get(REDIS_KEY_COINS,
                CoinData.class,
                new GetArgs().path(".data").indent("\t").newLine("\n").space(" "));
        return coinData.getCoins();
    }

    private void storeCoinsToRedisJSON(Coins coins){
        redisJSON.set(REDIS_KEY_COINS, SetArgs.Builder.create(".", GsonUtils.toJson(coins)));
    }

    public List<CoinInfo> fetchAllCoinsFromRedisJSON() {
        return getAllCoinsFromRedisJSON();
    }

    public List<Sample.Value> fetchCoinHistoryPerTimePeriodFromRedisTS(String symbol,
          String timePeriod) {
        Map<String, Object> tsInfo = fetchTSInfoForSymbol(symbol, timePeriod);
        Long firstTimeStamp = Long.valueOf(tsInfo.get("firstTimestamp").toString());
        Long lastTimeStamp = Long.valueOf(tsInfo.get("lastTimestamp").toString());

        List<Sample.Value> coinTSData =
                fetchTSDataForCoin(symbol, timePeriod, firstTimeStamp, lastTimeStamp);
        return coinTSData;

    }

    private List<Sample.Value> fetchTSDataForCoin(String symbol,
            String timePeriod, Long firstTimeStamp, Long lastTimeStamp) {
        String key = symbol + ":" + timePeriod;
        return redisTimeSeries.range(key, firstTimeStamp, lastTimeStamp);
    }

    private Map<String, Object> fetchTSInfoForSymbol(String symbol, String timePeriod) {
        return redisTimeSeries.info(symbol + ":" + timePeriod);
    }
}
