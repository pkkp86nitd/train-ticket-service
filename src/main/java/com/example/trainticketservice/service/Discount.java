package com.example.trainticketservice.service;

import java.util.Arrays;

public enum Discount {

    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);
    private final Integer val;


    Discount(Integer percentage) {
        this.val = percentage;
    }

    public Integer getPercentage() {
        return val;
    }

    public static boolean isValid(String inputVal) {
        return Arrays.stream(Discount.values())
                .anyMatch(discount -> discount.name().equals(inputVal));
    }


}
