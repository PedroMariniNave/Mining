package com.zpedroo.mining.cache;

import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.objects.Blocks;
import org.bukkit.Material;

import java.math.BigInteger;
import java.util.HashMap;

public class BlocksCache {

    private HashMap<Material, Blocks> blocksCache;

    public BlocksCache(FileManager file) {
        this.blocksCache = new HashMap<>();

        for (String str : file.get().getConfigurationSection("Blocks").getKeys(false)) {
            BigInteger coins = new BigInteger(file.get().getString("Blocks." + str + ".coins"));
            BigInteger tokens = new BigInteger(file.get().getString("Blocks." + str + ".tokens"));
            boolean luckyBlock = file.get().getBoolean("Blocks." + str + ".lucky-block");
            Material material = Material.getMaterial(str);
            Blocks block = new Blocks(coins, tokens, luckyBlock);
            blocksCache.put(material, block);
        }
    }

    public boolean isMineBlock(Material material) {
        if (material == null) return false;
        
        return blocksCache.containsKey(material);
    }

    public Blocks getBlockInfo(Material material) {
        return blocksCache.get(material);
    }
}