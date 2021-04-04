package com.zpedroo.mining.utils.chat;

import com.zpedroo.mining.enchants.Enchant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerChat {

    private Player player;
    private ItemStack item;
    private Enchant enchant;

    public PlayerChat(Player player, ItemStack item, Enchant enchant) {
        this.player = player;
        this.item = item;
        this.enchant = enchant;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public Enchant getEnchant() {
        return enchant;
    }
}