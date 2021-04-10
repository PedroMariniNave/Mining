package com.zpedroo.mining.commands;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class UpgraderCmd implements CommandExecutor {

    private FileManager file;

    public UpgraderCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("upgrader.admin") || args.length < 3) return true;

        String type = args[1];
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !isNumber(args[2]) || file.get().getString("UpgradersItem." + type) == null) return true;

        int amount = Integer.parseInt(args[2]);
        ItemStack item = ItemBuilder.build(file, "UpgradersItem." + type, new String[]{
                "{upgrade}"
        }, new String[]{
                NumberFormatter.fixDecimal(file.get().getInt("UpgradersItem." + type + ".upgrade"))
        });

        item.setAmount(amount);
        NBTItem nbt = new NBTItem(item);
        nbt.setInteger("upgrader_amount", file.get().getInt("UpgradersItem." + type + ".upgrade"));

        target.getInventory().addItem(nbt.getItem());
        return false;
    }

    private boolean isNumber(String str) {
        if (str == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str).matches();
    }
}
