package com.zpedroo.mining.objects;

import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PlayerBooster {

    private Player player;
    private String type;
    private int multiplier;
    private long endTime;

    public PlayerBooster(Player player, String type, int multiplier, int duration) {
        this.player = player;
        this.type = type;
        this.multiplier = multiplier;
        this.endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(duration);
    }

    public Player getPlayer() {
        return player;
    }

    public String getType() {
        return type;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public long getEndTime() {
        return endTime;
    }
}