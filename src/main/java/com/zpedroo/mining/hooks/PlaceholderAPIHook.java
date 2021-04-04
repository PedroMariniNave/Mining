package com.zpedroo.mining.hooks;

import com.zpedroo.mining.Main;
import com.zpedroo.mining.data.PlayerData;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private Main main;

    public PlaceholderAPIHook(Main main) {
        this.main = main;
    }

    public String getAuthor() {
        return main.getDescription().getAuthors().toString();
    }

    public String getIdentifier() {
        return "mining";
    }

    public String getVersion() {
        return main.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        PlayerData data = Main.get().getDataManager().loadPlayer(player);
        switch (identifier) {
            case "avaible":
                return NumberFormatter.formatNumber(data.getBlocksAvaible().doubleValue());
            case "broken":
                return NumberFormatter.formatNumber(data.getBlocksBroken().doubleValue());
            case "tokens":
                return NumberFormatter.formatNumber(data.getTokens().doubleValue());
        }
        return null;
    }
}