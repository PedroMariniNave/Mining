package com.zpedroo.mining.managers;

import com.zpedroo.mining.objects.PlayerBooster;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerBoosterManager {

    public HashMap<Player, List<PlayerBooster>> boosters;

    public PlayerBoosterManager() {
        this.boosters = new HashMap<>(64);
    }

    public void setBooster(Player player, String type, int multiplier, int duration) {
        PlayerBooster playerBooster = new PlayerBooster(player, type, multiplier, duration);
        List<PlayerBooster> boosters;
        if (getBoosters().containsKey(player)) {
            boosters = getBoosters().get(player);
        } else {
            boosters = new ArrayList<>();
        }
        boosters.add(playerBooster);
        getBoosters().put(player, boosters);
    }

    public void removeBooster(Player player, String type) {
        getBoosters().get(player).removeIf(playerBooster -> playerBooster.getType().equals(type));
    }

    public boolean hasBoosterActived(Player player, String type) {
        if (!getBoosters().containsKey(player)) return false;

        AtomicBoolean actived = new AtomicBoolean(false);

        getBoosters().get(player).forEach(playerBooster -> {
            if (StringUtils.equals(playerBooster.getType(), type)) {
                actived.set(true);
            }
        });
        return actived.get();
    }

    public PlayerBooster getPlayerBooster(Player player, String type) {
        for (PlayerBooster playerBooster : getBoosters().get(player)) {
            if (StringUtils.equalsIgnoreCase(playerBooster.getType(), type)) {
                return playerBooster;
            }
        }
        return null;
    }

    public HashMap<Player, List<PlayerBooster>> getBoosters() {
        return boosters;
    }
}