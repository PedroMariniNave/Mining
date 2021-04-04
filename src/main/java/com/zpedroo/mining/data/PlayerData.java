package com.zpedroo.mining.data;

import java.math.BigInteger;
import java.util.UUID;

public class PlayerData{

    private UUID uuid;
    private BigInteger broken;
    private BigInteger avaible;
    private BigInteger tokens;

    public PlayerData(UUID uuid, BigInteger broken, BigInteger avaible, BigInteger tokens) {
        this.uuid = uuid;
        this.broken = broken;
        this.avaible = avaible;
        this.tokens = tokens;
    }

    public UUID getUUID() {
        return uuid;
    }

    public BigInteger getBlocksBroken() {
        return broken;
    }

    public BigInteger getBlocksAvaible() {
        return avaible;
    }

    public BigInteger getTokens() {
        return tokens;
    }

    public void setBlocksBroken(BigInteger broken) {
        this.broken = broken;
    }

    public void setBlocksAvaible(BigInteger avaible) {
        this.avaible = avaible;
    }

    public void setTokens(BigInteger tokens) {
        this.tokens = tokens;
    }
}