package com.paathshala.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryData {
    private String timestamp;
    private double value;

    public HistoryData(String timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
