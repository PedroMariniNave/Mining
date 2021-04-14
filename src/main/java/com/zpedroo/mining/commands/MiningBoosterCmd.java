package com.zpedroo.mining.commands;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class MiningBoosterCmd implements CommandExecutor {

    private FileManager file;

    public MiningBoosterCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mining.admin")) return true;

        if (args.length < 6) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.booster-usage")));
            return true;
        }

        switch (args[0].toUpperCase()) {
            case "GIVE":
                if (!isNumber(args[3]) || !isNumber(args[4]) || !isNumber(args[5])) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.invalid-value")));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.offline-player")));
                    return true;
                }

                String type = args[2].toUpperCase();

                if (!StringUtils.equals(type, "REWARDS") && !StringUtils.equals(type, "TREASURES") && !StringUtils.equals(type, "BLOCKS")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.invalid-booster")));
                    return true;
                }

                int multiplier = Integer.parseInt(args[3]);
                int duration = Integer.parseInt(args[4]);
                int amount = Integer.parseInt(args[5]);

                ItemStack item = ItemBuilder.build(file, "Booster", new String[]{
                        "{type}",
                        "{multiplier}",
                        "{duration}"
                }, new String[]{
                        translateBooster(type),
                        String.valueOf(multiplier),
                        String.valueOf(duration)
                });

                item.setAmount(amount);

                NBTItem nbt = new NBTItem(item);

                nbt.setString("mining_booster", new StringBuilder(type).append("#").append(multiplier).append("#").append(duration).toString());

                target.getInventory().addItem(nbt.getItem());
                break;
        }
        return false;
    }

    private String translateBooster(String type) {
        return StringUtils.replaceEach(type, new String[]{
                "REWARDS",
                "TREASURES",
                "BLOCKS"
        }, new String[]{
                "Recompensas",
                "Tesouros",
                "Blocos"
        });
    }

    private boolean isNumber(String str) {
        if (str == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str).matches();
    }
}
