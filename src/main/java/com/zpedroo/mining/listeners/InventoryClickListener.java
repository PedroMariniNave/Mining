package com.zpedroo.mining.listeners;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.Main;
import com.zpedroo.mining.enchants.Enchant;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.item.ItemUtils;
import com.zpedroo.mining.utils.item.PickaxeUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class InventoryClickListener implements Listener {

    private ItemUtils itemUtils;
    private ArrayList<String> TITLES;

    public InventoryClickListener(ItemUtils itemUtils, FileManager file) {
        this.itemUtils = itemUtils;
        this.TITLES = new ArrayList<>(4);
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

        if (event.getClickedInventory() == null || event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        if (event.getClickedInventory().getType().equals(InventoryType.PLAYER) && event.getCursor().getType() != Material.AIR){
            NBTItem cursorNBT = new NBTItem(event.getCursor());
            NBTItem currentNBT = new NBTItem(event.getCurrentItem());
            if (!cursorNBT.hasNBTData() || !currentNBT.hasNBTData()) return;

            if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR) && cursorNBT.hasKey("upgrader_amount")) {
                if (!event.getCurrentItem().getType().toString().endsWith("_PICKAXE")) return;

                event.setCancelled(true);
                int upgrade = cursorNBT.getInteger("upgrader_amount") * event.getCursor().getAmount();
                event.setCursor(new ItemStack(Material.AIR));

                PickaxeUtils pickaxe = new PickaxeUtils(event.getCurrentItem());
                for (Enchant enchant : Main.get().getEnchantsCache().getEnchants()) {
                    if ((pickaxe.getEnchantLevel(enchant) + upgrade) > enchant.getMaxLevel()) {
                        pickaxe.setEnchant(enchant, enchant.getMaxLevel());
                        continue;
                    }

                    pickaxe.setEnchant(enchant, pickaxe.getEnchantLevel(enchant) + upgrade);
                }

                player.getInventory().setItem(event.getSlot(), pickaxe.create());
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
            }
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