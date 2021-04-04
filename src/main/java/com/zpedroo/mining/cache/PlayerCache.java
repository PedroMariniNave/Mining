package com.zpedroo.mining.cache;

import com.zpedroo.mining.Main;
import com.zpedroo.mining.hooks.VaultHook;
import com.zpedroo.mining.managers.FileManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.HashMap;

public class PlayerCache {

    private FileManager file;
    private HashMap<Player, BigInteger> coinsCache;
    private HashMap<Player, BigInteger> tokensCache;

    public PlayerCache(FileManager file) {
        this.file = file;
        this.coinsCache = new HashMap<>();
        this.tokensCache = new HashMap<>();
        this.setupTask();
    }

    public HashMap<Player, BigInteger> getCoinsCache() {
        return coinsCache;
    }

    public HashMap<Player, BigInteger> getTokensCache() {
        return tokensCache;
    }

    public BigInteger getCoinsCache(Player player) {
        return coinsCache.getOrDefault(player, BigInteger.ZERO);
    }

    public BigInteger getTokensCache(Player player) {
        return tokensCache.getOrDefault(player, BigInteger.ZERO);
    }

    private void setupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getCoinsCache().forEach((player, coins) -> {
                    if (player == null) return;

                    VaultHook.giveMoney(player, coins.doubleValue());
                });
                getTokensCache().forEach((player, tokens) -> {
                    if (player == null) return;

                    Main.get().getDataManager().loadPlayer(player).setTokens(Main.get().getDataManager().loadPlayer(player).getTokens().add(tokens));
                });

                getCoinsCache().clear();
                getTokensCache().clear();
            }
        }.runTaskTimerAsynchronously(Main.get(), 20L * file.get().getInt("Settings.task-interval"), 20L * file.get().getInt("Settings.task-interval"));
    }
}