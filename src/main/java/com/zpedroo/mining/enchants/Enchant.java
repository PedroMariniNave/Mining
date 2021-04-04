package com.zpedroo.mining.enchants;

import java.math.BigInteger;

public class Enchant {

    private String name;
    private Integer maxLevel;
    private BigInteger pricePerLevel;

    public Enchant(String name, Integer maxLevel, BigInteger pricePerLevel) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.pricePerLevel = pricePerLevel;
    }

    public String getName() {
        return name;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public BigInteger getPricePerLevel() {
        return pricePerLevel;
    }
}