package com.zpedroo.mining.listeners;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.item.PickaxeUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RegionListeners implements Listener {

    private FileManager file;
    private List<String> mineRegions;

    public RegionListeners(FileManager file) {
        this.file = file;
        this.mineRegions = new ArrayList<>();
        this.mineRegions.addAll(file.get().getStringList("Regions.enter"));
    }

    @EventHandler
    public void onEnter(RegionEnterEvent event) {
        if (getMineRegions().contains(event.getRegion().getId())) {
            if (!hasPickaxe(event.getPlayer())) {
                String[] enchants = new String[]{};
                int[] levels = new int[]{};
                for (String str : file.get().getConfigurationSection("Enchants").getKeys(false)) {
                    enchants = addElementToArray(enchants.length, enchants, str);
                    levels = addElementToArray(levels.length, levels, file.get().getInt("Enchants." + str + ".initial-level"));
                }

                event.getPlayer().getInventory().addItem(new PickaxeUtils(enchants, levels).create());
            }
        }
    }

    private boolean hasPickaxe(Player player) {
        boolean found = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            if (item.getType().toString().endsWith("_PICKAXE")) found = true;
        }
        return found;
    }

    private List<String> getMineRegions() {
        return mineRegions;
    }

    private int[] addElementToArray(int lenght, int[] arr, int element) {
        int[] newArray = new int[lenght + 1];
        if (lenght >= 0) System.arraycopy(arr, 0, newArray, 0, lenght);
        newArray[lenght] = element;
        return newArray;
    }

    private String[] addElementToArray(int lenght, String[] arr, String element) {
        String[] newArray = new String[lenght + 1];
        if (lenght >= 0) System.arraycopy(arr, 0, newArray, 0, lenght);
        newArray[lenght] = element;
        return newArray;
    }
}
