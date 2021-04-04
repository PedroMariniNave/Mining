package com.zpedroo.mining.commands;

import com.zpedroo.mining.managers.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class MiningAdminCmd implements CommandExecutor {

    private FileManager file;

    public MiningAdminCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.isOp()) return true;

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.mining-usage")));
            return true;
        }

        switch (args[0].toUpperCase()) {
            case "SETUPGRADE":
                Block block = player.getTargetBlock((HashSet<Byte>) null, 5);
                Location location = block.getLocation();
                file.get().set("Upgrade-Location", serializeLocation(location));
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