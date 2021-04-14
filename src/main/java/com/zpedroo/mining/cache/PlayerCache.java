package com.zpedroo.mining.cache;

import com.zpedroo.mining.Main;
import com.zpedroo.mining.hooks.VaultHook;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.objects.PlayerBooster;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.HashMap;

public class PlayerCache {

    private FileManager file;
    private HashMap<Player, BigInteger> coinsCache;
    private HashMap<Player, BigInteger> tokensCache;
    private HashMap<Player, PlayerBooster> boostersToRemove;

    public PlayerCache(FileManager file) {
        this.file = file;
        this.coinsCache = new HashMap<>(32);
        this.tokensCache = new HashMap<>(32);
        this.boostersToRemove = new HashMap<>(32);
        this.setupTask();
    }

    public HashMap<Player, BigInteger> getCoinsCache() {
        return coinsCache;
    }

    public HashMap<Player, BigInteger> getTokensCache() {
        return tokensCache;
    }

    public HashMap<Player, PlayerBooster> getBoostersToRemove() {
        return boostersToRemove;
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

                Main.get().getPlayerBoosterManager().getBoosters().forEach((player, playerBoosters) -> {
                    playerBoosters.forEach(playerBooster -> {
                        if (System.currentTimeMillis() >= playerBooster.getEndTime()) {
                            getBoostersToRemove().put(player, playerBooster);
                            if (playerBooster.getPlayer() == null) return;

                            playerBooster.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.replaceEach(file.get().getString("Messages.booster-ended"), new String[]{
                                    "{type}"
                            }, new String[]{
                                    StringUtils.replaceEach(playerBooster.getType(), new String[]{
                                            "REWARDS",
                                            "TREASURES",
                                            "BLOCKS"
                                    }, new String[]{
                                            "Recompensas",
                                            "Tesouros",
                                            "Blocos"
                                    })
                            })));
                            playerBooster.getPlayer().playSound(playerBooster.getPlayer().getLocation(), Sound.EXPLODE, 10, 10);
                        }
                    });
                });

                for (PlayerBooster playerBooster : getBoostersToRemove().values()) {
                    Main.get().getPlayerBoosterManager().removeBooster(playerBooster.getPlayer(), playerBooster.getType());
                }

                getBoostersToRemove().clear();
                getCoinsCache().clear();
                getTokensCache().clear();
            }
        }.runTaskTimerAsynchronously(Main.get(), 20L * file.get().getInt("Settings.task-interval"), 20L * file.get().getInt("Settings.task-interval"));
    }
}