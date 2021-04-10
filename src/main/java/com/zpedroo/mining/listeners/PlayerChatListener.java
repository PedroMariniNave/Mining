package com.zpedroo.mining.listeners;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.zpedroo.mining.Main;
import com.zpedroo.mining.data.PlayerData;
import com.zpedroo.mining.enchants.Enchant;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.chat.PlayerChat;
import com.zpedroo.mining.utils.item.PickaxeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.regex.Pattern;

public class PlayerChatListener implements Listener {

    private FileManager file;
    private static HashMap<Player, PlayerChat> playerChatMap;

    public PlayerChatListener(FileManager file) {
        this.file = file;
        this.playerChatMap = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(ChatMessageEvent event) {
        if (!getPlayerChat().containsKey(event.getSender())) return;

        event.setCancelled(true);
        PlayerChat playerChat = getPlayerChat().remove(event.getSender());
        Player player = playerChat.getPlayer();
        String msg = event.getMessage();

        if (!isNumber(msg)) {
            player.sendMessage(getColored(file.get().getString("Messages.upgrade-cancelled")));
            return;
        }

        Enchant enchant = playerChat.getEnchant();
        PickaxeUtils pickaxeUtils = new PickaxeUtils(playerChat.getItem());
        int levelToUpgrade = Integer.parseInt(msg);
        if ((levelToUpgrade + pickaxeUtils.getEnchantLevel(enchant)) > enchant.getMaxLevel()) {
            player.sendMessage(getColored(file.get().getString("Messages.upgrade-maximum-level")));
            return;
        }

        PlayerData data = Main.get().getDataManager().loadPlayer(player);
        BigInteger price = enchant.getPricePerLevel();

        if (data.getTokens().compareTo(price.multiply(BigInteger.valueOf(levelToUpgrade))) >= 0) {
            data.setTokens(data.getTokens().subtract(price.multiply(BigInteger.valueOf(levelToUpgrade))));
            pickaxeUtils.setEnchant(enchant, pickaxeUtils.getEnchantLevel(enchant) + levelToUpgrade);
            player.setItemInHand(pickaxeUtils.create());
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
        } else {
            player.sendMessage(getColored(file.get().getString("Messages.insufficient-tokens")));
        }
    }

    public static HashMap<Player, PlayerChat> getPlayerChat() {
        return playerChatMap;
    }

    private String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private boolean isNumber(String str) {
        if (str == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str).matches();
    }
}