package com.zpedroo.mining.commands;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.regex.Pattern;

public class MiningAdminCmd implements CommandExecutor {

    private FileManager file;

    public MiningAdminCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("mining.admin")) return true;

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.mining-usage")));
            return true;
        }

        Block block = player.getTargetBlock((HashSet<Byte>) null, 5);
        Location location = block.getLocation();
        switch (args[0].toUpperCase()) {
            case "SETUPGRADE":
                file.get().set("Upgrade-Location", serializeLocation(location));
                file.save();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.location-set")));
                break;
            case "SETTRADE":
                file.get().set("Trade-Location", serializeLocation(location));
                file.save();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.location-set")));
                break;
            case "RELOAD":
                file.reload();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.config-reloaded")));
                break;
        }
        return false;
    }

    private String serializeLocation(Location location) {
        return new StringBuilder()
                .append(location.getWorld().getName()).append("#")
                .append((int) location.getX()).append("#")
                .append((int) location.getY()).append("#")
                .append((int) location.getZ())
                .toString();
    }
}