package com.zpedroo.mining.utils.item;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.Main;
import com.zpedroo.mining.enchants.Enchant;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PickaxeUtils {

    private HashMap<Enchant, Integer> enchants = new HashMap<>();
    private int level;
    private short durability;

    public PickaxeUtils(ItemStack item) {
        this.durability = item.getDurability();
        this.update(item);
    }

    public PickaxeUtils(String[] enchants, int[] levels) {
        this.level = 0;
        int i = -1;
        for (String str : enchants) {
            this.enchants.put(getEnchant(str), levels[++i]);
        }
        for (int level : levels) {
            this.level += level;
        }
    }

    public ItemStack create() {
        ItemStack item = ItemBuilder.build(Main.get().getFiles().get("CONFIG"), "Pickaxe", new String[]{
                "{dig_speed}",
                "{durability}",
                "{loot_bonus_blocks}",
                "{super_area}",
                "{destruction}",
                "{lucky}",
                "{level}"
        }, new String[]{
                String.valueOf(getEnchantLevel("DIG_SPEED")),
                String.valueOf(getEnchantLevel("DURABILITY")),
                String.valueOf(getEnchantLevel("LOOT_BONUS_BLOCKS")),
                String.valueOf(getEnchantLevel("SUPER_AREA")),
                String.valueOf(getEnchantLevel("DESTRUCTION")),
                String.valueOf(getEnchantLevel("LUCKY")),
                NumberFormatter.fixDecimal(getLevel())
        });
        item.setDurability(this.durability);
        NBTItem nbt = new NBTItem(item);

        for (Enchant enchant : getEnchants().keySet()) {
            nbt.setInteger("pick_" + enchant.getName().toLowerCase(), getEnchantLevel(enchant));
        }

        return nbt.getItem();
    }

    public void setEnchant(Enchant enchant, int level) {
        ItemStack pickaxe = this.create();
        NBTItem nbt = new NBTItem(pickaxe);
        nbt.setInteger("pick_" + enchant.getName().toLowerCase(), level);
        this.update(nbt.getItem());
    }

    private void update(ItemStack item) {
        this.level = 0;
        NBTItem nbt = new NBTItem(item);
        for (Enchant enchant : Main.get().getEnchantsCache().getEnchants()) {
            getEnchants().put(enchant, nbt.getInteger("pick_" + enchant.getName().toLowerCase()));
        }
        getEnchants().forEach((enchant, integer) -> {
            level += integer;
        });
    }

    private HashMap<Enchant, Integer> getEnchants() {
        return enchants;
    }

    public int getEnchantLevel(Enchant enchant) {
        return getEnchants().getOrDefault(enchant, 0);
    }

    private int getEnchantLevel(String enchant) {
        return getEnchants().getOrDefault(Main.get().getEnchantsCache().getEnchant(enchant), 0);
    }

    private int getLevel() {
        return level;
    }

    public Enchant getEnchant(String enchant) {
        return Main.get().getEnchantsCache().getEnchant(enchant);
    }
}