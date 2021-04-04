package com.zpedroo.mining.objects;

import java.math.BigInteger;

public class Blocks {

    private BigInteger coins;
    private BigInteger tokens;
    private Boolean luckyBlock;

    public Blocks(BigInteger coins, BigInteger tokens, Boolean luckyBlock) {
        this.coins = coins;
        this.tokens = tokens;
        this.luckyBlock = luckyBlock;
    }

    public BigInteger getCoins() {
        return coins;
    }

    public BigInteger getTokens() {
        return tokens;
    }

    public Boolean isLuckyBlock() {
        return luckyBlock;
    }
}