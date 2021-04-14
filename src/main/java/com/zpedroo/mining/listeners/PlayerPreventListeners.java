package com.zpedroo.mining.listeners;

import com.zpedroo.mining.managers.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.ArrayList;

public class PlayerPreventListeners implements Listener {

    private ArrayList<String> TITLES;

    public PlayerPreventListeners(FileManager file) {
        this.TITLES = new ArrayList<>(4);
        for (String str : file.get().getConfigurationSection("Inventories").getKeys(false)) {
            TITLES.add(ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories." + str + ".title")));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (TITLES.contains(event.getPlayer().getOpenInventory().getTitle()) || PlayerChatListener.getPlayerChat().containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (PlayerChatListener.getPlayerChat().containsKey(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
}