package com.zpedroo.mining.commands;

import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.Main;
import com.zpedroo.mining.data.PlayerData;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class TokensCmd implements CommandExecutor {

    private FileManager file;

    public TokensCmd(FileManager file) {
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) return true;

            Player player = (Player) sender;
            PlayerData data = Main.get().getDataManager().loadPlayer(player);
            player.sendMessage(replacePlaceholders(data, file.get().getString("Messages.tokens.message")));
            player.sendTitle(replacePlaceholders(data, file.get().getString("Messages.tokens.title")), replacePlaceholders(data, file.get().getString("Messages.tokens.subtitle")));
        }

        if (args.length >= 3) {
            if (!sender.hasPermission("tokens.admin")) return true;

            Player target = Bukkit.getPlayer(args[1]);
            PlayerData data = Main.get().getDataManager().loadPlayer(target);
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
                    data.setTokens(data.getTokens().add(amount));
                    break;

                case "SET":
                    data.setTokens(amount);
                    break;

                case "VALE":
                    if (hasTokensItem(target)) {
                        double finalAmount = amount.doubleValue() + new NBTItem(getTokensItem(target)).getDouble("tokens_amount");
                        ItemStack item = ItemBuilder.build(file, "Tokens", new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.formatNumber(finalAmount)
                        });

                        NBTItem nbt = new NBTItem(item);
                        nbt.setDouble("tokens_amount", finalAmount);

                        target.getInventory().removeItem(getTokensItem(target));
                        target.getInventory().addItem(nbt.getItem());
                    } else {
                        ItemStack item = ItemBuilder.build(file, "Tokens", new String[]{
                                "{amount}"
                        }, new String[]{
                                NumberFormatter.formatNumber(amount.doubleValue())
                        });
                        NBTItem nbt = new NBTItem(item);
                        nbt.setDouble("tokens_amount", amount.doubleValue());

                        target.getInventory().addItem(nbt.getItem());
                    }
                    break;
            }
        }
        return false;
    }

    private boolean hasTokensItem(Player player) {
        boolean found = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            NBTItem nbt = new NBTItem(item);
            if (!nbt.hasNBTData() || !nbt.hasKey("tokens_amount")|| item.getAmount() > 1) continue;

            found = true;
        }
        return found;
    }

    private ItemStack getTokensItem(Player player) {
        ItemStack item = null;
        for (ItemStack items : player.getInventory().getContents()) {
            if (items == null || items.getType().equals(Material.AIR)) continue;

            NBTItem nbt = new NBTItem(items);
            if (!nbt.hasNBTData() || !nbt.hasKey("tokens_amount") || items.getAmount() > 1) continue;

            item = items;
            break;
        }
        return item;
    }

    private String replacePlaceholders(PlayerData data, String str) {
        return ChatColor.translateAlternateColorCodes('&', StringUtils.replaceEach(str, new String[]{
                "{tokens}",
        }, new String[]{
                NumberFormatter.formatNumber(data.getTokens().doubleValue()),
        }));
    }

    private boolean isNumber(String str) {
        if (str == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(str).matches();
    }
}