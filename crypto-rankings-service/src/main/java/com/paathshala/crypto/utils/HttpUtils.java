package com.paathshala.crypto.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class HttpUtils {

    private static String apiHost = "coinranking1.p.rapidapi.com";
    private static String apiKey = "";

    public static HttpEntity<String> getHttpEntity() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("X-RapidAPI-Host", apiHost);
        httpHeaders.set("X-RapidAPI-Key", apiKey);
        return new HttpEntity<>(null, httpHeaders);
    }
}
