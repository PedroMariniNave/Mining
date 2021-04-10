package com.zpedroo.mining.commands;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class BlocksCmd implements CommandExecutor {

    private FileManager file;

    public BlocksCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 3) {
            if (!sender.hasPermission("blocks.admin")) return true;

            Player target = Bukkit.getPlayer(args[1]);
            BigInteger amount = new BigInteger(args[2]);

            if (target == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.offline-player")));
                return true;
            }

            if (!isNumber(args[2]) || amount.signum() < 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.invalid-value")));
                return true;
            }

            switch (args[0].toUpperCase()) {
                case "GIVE":
                    ItemStack item = ItemBuilder.build(file, "BlocksItem", new String[]{
                            "{amount}"
                    }, new String[]{
                            NumberFormatter.formatNumber(amount.doubleValue())
                    });
                    NBTItem nbt = new NBTItem(item);
                    nbt.setDouble("blocks_amount", amount.doubleValue());

                    target.getInventory().addItem(nbt.getItem());
                    break;
            }
        }
        return false;
    }

    private boolean isNumber(String str) {
        if (str == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str).matches();
    }
}
