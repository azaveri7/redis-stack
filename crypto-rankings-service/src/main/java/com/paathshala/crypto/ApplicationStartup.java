package com.paathshala.crypto;

import com.paathshala.crypto.service.CoinsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private CoinsDataService coinsDataService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        // We can comment the code once the data is stored in Redis
        coinsDataService.fetchCoins();
        coinsDataService.fetchCoinHistory();
    }
}
