package com.paathshala.crypto.service;

import com.paathshala.crypto.model.CoinData;
import com.paathshala.crypto.model.CoinInfo;
import com.paathshala.crypto.model.Coins;
import com.paathshala.crypto.utils.HttpUtils;
import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import org.slf4j.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CoinsDataService {

    public static final String GET_COINS_API = "https://coinranking1.p.rapidapi.com/coins?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h&tiers%5B0%5D=1&orderBy=marketCap&orderDirection=desc&limit=50&offset=0";
    private static final Logger log = LoggerFactory.getLogger(CoinsDataService.class);
    private static final String REDIS_KEY_COINS = "coins";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisJSON redisJSON;

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
}
