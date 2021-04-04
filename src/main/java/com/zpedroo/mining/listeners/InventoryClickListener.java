package com.zpedroo.mining.listeners;

import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.item.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;

public class InventoryClickListener implements Listener {

    private ItemUtils itemUtils;
    private ArrayList<String> TITLES;

    public InventoryClickListener(ItemUtils itemUtils, FileManager file) {
        this.itemUtils = itemUtils;
        this.TITLES = new ArrayList<>();
        for (String str : file.get().getConfigurationSection("Inventories").getKeys(false)) {
            TITLES.add(ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories." + str + ".title")));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (TITLES.contains(event.getInventory().getName())) {
            event.setCancelled(true);
            if (getItemUtils().getInventoryActions(event.getClickedInventory()) == null || event.getClickedInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR) || event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

            runAction(getItemUtils().getAction(event.getClickedInventory(), event.getSlot()));
        }
    }

    private void runAction(Runnable action) {
        if (action == null) return;

        action.run();
    }

    private ItemUtils getItemUtils() {
        return itemUtils;
    }
}