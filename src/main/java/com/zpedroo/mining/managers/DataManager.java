package com.zpedroo.mining.managers;

import com.zpedroo.mining.Main;
import com.zpedroo.mining.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DataManager{

    private HashMap<Player, PlayerData> playerDataCache;
    private List<PlayerData> topTenCache;

    public DataManager() {
        this.playerDataCache = new HashMap<>(512);
        this.topTenCache = new LinkedList<>();
        this.updateTopTen();
        this.setupTask();
    }

    public PlayerData loadPlayer(Player player) {
        if (player == null) return null;
        if (getCache().containsKey(player)) return getCache().get(player);

        PlayerData data = Main.get().getSQLiteConnector().loadPlayer(player.getUniqueId());
        getCache().put(player, data);
        return data;
    }

    public void savePlayer(Player player) {
        if (getCache().containsKey(player)) {
            PlayerData data = getCache().get(player);
            boolean saved = Main.get().getSQLiteConnector().savePlayer(data);
            if (saved) getCache().remove(player);
        }
    }

    public void saveAll() {
        getCache().values().forEach(data -> {
            Main.get().getSQLiteConnector().savePlayer(data);
        });
    }

    private void updateTopTen() {
        this.topTenCache = Main.get().getSQLiteConnector().getTopTen();
    }

    private void setupTask() {
        Main.get().getServer().getScheduler().runTaskTimerAsynchronously(Main.get(), this::updateTopTen, 20L * Main.get().getFiles().get("CONFIG").get().getInt("Settings.update-interval"), 20L * Main.get().getFiles().get("CONFIG").get().getInt("Settings.update-interval"));
    }

    public HashMap<Player, PlayerData> getCache() {
        return playerDataCache;
    }

    public List<PlayerData> getTopTenCache() {
        return topTenCache;
    }
}