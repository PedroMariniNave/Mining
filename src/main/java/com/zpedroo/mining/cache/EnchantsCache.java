package com.zpedroo.mining.cache;

import com.zpedroo.mining.enchants.Enchant;
import com.zpedroo.mining.managers.FileManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EnchantsCache {

    private List<Enchant> enchants;

    public EnchantsCache(FileManager file) {
        this.enchants = new ArrayList<>();

        for (String enchant : file.get().getConfigurationSection("Enchants").getKeys(false)) {
            int maxLevel = file.get().getInt("Enchants." + enchant + ".max-level");
            BigInteger pricePerLevel = new BigInteger(file.get().getString("Enchants." + enchant + ".price-per-level"));
            enchants.add(new Enchant(enchant, maxLevel, pricePerLevel));
        }
    }

    public List<Enchant> getEnchants() {
        return enchants;
    }

    public Enchant getEnchant(String enchant) {
        for (Enchant ench : getEnchants()) {
            if (ench.getName().equals(enchant)) {
                return ench;
            }
        }
        return null;
    }
}