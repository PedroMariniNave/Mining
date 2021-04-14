package com.zpedroo.mining.listeners;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skydhs.voltzspawners.utils.nbti.NBTItem;
import com.zpedroo.mining.Main;
import com.zpedroo.mining.data.PlayerData;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.objects.Blocks;
import com.zpedroo.mining.utils.builder.ItemBuilder;
import com.zpedroo.mining.utils.chat.PlayerChat;
import com.zpedroo.mining.utils.formatter.NumberFormatter;
import com.zpedroo.mining.utils.item.PickaxeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerGeneralListeners implements Listener {

    private FileManager file;
    private List<String> mineRegions;
    private List<String> miningRegions;
    private List<String> blacklistedRegions;

    public PlayerGeneralListeners(FileManager file) {
        this.file = file;
        this.mineRegions = new ArrayList<>(4);
        this.miningRegions = new ArrayList<>(4);
        this.blacklistedRegions = new ArrayList<>(4);
        this.mineRegions.addAll(file.get().getStringList("Regions.enter"));
        this.miningRegions.addAll(file.get().getStringList("Regions.mine"));
        this.blacklistedRegions.addAll(file.get().getStringList("Regions.blacklist"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public synchronized void onBreak(BlockBreakEvent event) {
        for (ProtectedRegion region : WGBukkit.getRegionManager(event.getBlock().getWorld()).getApplicableRegions(event.getBlock().getLocation())) {
            if (getMiningRegions().contains(region.getId())) {
                Player player = event.getPlayer();
                if (!Main.get().getBlocks().isMineBlock(event.getBlock().getType()) || player.getItemInHand() == null || !player.getItemInHand().getType().toString().endsWith("_PICKAXE")) return;

                PlayerData data = Main.get().getDataManager().loadPlayer(player);
                PickaxeUtils pickaxeUtils = new PickaxeUtils(player.getItemInHand());
                Blocks block = Main.get().getBlocks().getBlockInfo(event.getBlock().getType());
                BigInteger blocks = BigInteger.ONE;
                BigInteger coins = block.getCoins();
                BigInteger tokens = block.getTokens();
                int fortune = pickaxeUtils.getEnchantLevel(Main.get().getEnchantsCache().getEnchant("LOOT_BONUS_BLOCKS"));
                int destruction = pickaxeUtils.getEnchantLevel(Main.get().getEnchantsCache().getEnchant("DESTRUCTION"));
                int superArea = pickaxeUtils.getEnchantLevel(Main.get().getEnchantsCache().getEnchant("SUPER_AREA"));
                int thunder = pickaxeUtils.getEnchantLevel(Main.get().getEnchantsCache().getEnchant("THUNDER"));
                int lucky = pickaxeUtils.getEnchantLevel(Main.get().getEnchantsCache().getEnchant("LUCKY"));
                boolean luckyBlock = block.isLuckyBlock();

                // Destruction enchant
                if (new Random().nextInt(1500 + 1) <= destruction && destruction > 0 && !getBlacklistedRegions().contains(region.getId())) {
                    int i = 0;
                    for (int xOff = -55; xOff <= 55; ++xOff) {
                        for (int zOff = -55; zOff <= 55; ++zOff) {
                            Block blockFound = event.getBlock().getRelative(xOff, 0, zOff);
                            if (!Main.get().getBlocks().isMineBlock(blockFound.getType())) continue;

                            blocks = blocks.add(BigInteger.ONE);
                            coins = coins.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getCoins());
                            tokens = tokens.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getTokens());

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    blockFound.breakNaturally();
                                    blockFound.getState().update();
                                }
                            }.runTaskLaterAsynchronously(Main.get(), ++i/50);
                        }
                    } // Super Area enchant
                } else if (new Random().nextInt(1500 + 1) <= superArea && superArea > 0 && !getBlacklistedRegions().contains(region.getId())) {
                    for (int xOff = -1; xOff <= 1; ++xOff) {
                        for (int yOff = -1; yOff <= 1; ++yOff) {
                            for (int zOff = -1; zOff <= 1; ++zOff) {
                                Block blockFound = event.getBlock().getRelative(xOff, yOff, zOff);
                                if (!Main.get().getBlocks().isMineBlock(blockFound.getType())) continue;

                                blocks = blocks.add(BigInteger.ONE);
                                coins = coins.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getCoins());
                                tokens = tokens.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getTokens());

                                blockFound.breakNaturally();
                                blockFound.getState().update();
                            }
                        }
                    }
                } else if (new Random().nextInt(1500 + 1) <= thunder && thunder > 0 && !getBlacklistedRegions().contains(region.getId())) {
                    Bukkit.getWorld(event.getBlock().getWorld().getName()).strikeLightningEffect(event.getBlock().getLocation());
                    for (int yOff = -50; yOff <= 50; ++yOff) {
                        Block blockFound = event.getBlock().getRelative(0, yOff, 0);
                        if (!Main.get().getBlocks().isMineBlock(blockFound.getType())) continue;

                        blocks = blocks.add(BigInteger.ONE);
                        coins = coins.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getCoins());
                        tokens = tokens.add(Main.get().getBlocks().getBlockInfo(blockFound.getType()).getTokens());

                        blockFound.breakNaturally();
                        blockFound.getState().update();
                    }
                }

                if (Main.get().getPlayerBoosterManager().hasBoosterActived(player, "REWARDS")) {
                    int multiplier = Main.get().getPlayerBoosterManager().getPlayerBooster(player, "REWARDS").getMultiplier();
                    coins = coins.multiply(new BigInteger(String.valueOf(multiplier)));
                    tokens = tokens.multiply(new BigInteger(String.valueOf(multiplier)));
                }

                if (Main.get().getPlayerBoosterManager().hasBoosterActived(player, "BLOCKS")) {
                    int multiplier = Main.get().getPlayerBoosterManager().getPlayerBooster(player, "BLOCKS").getMultiplier();
                    blocks = blocks.multiply(new BigInteger(String.valueOf(multiplier)));
                }

                // Fortune enchant
                if (fortune > 0) {
                    coins = coins.add(coins.multiply(BigInteger.valueOf(fortune)));
                    tokens = tokens.add(tokens.multiply(BigInteger.valueOf(fortune)));
                }
                /*
                // If is a lucky block, treasure chance is 100%, so we'll select a random treasure
                if (luckyBlock) {
                    int size = file.get().getConfigurationSection("Treasures").getKeys(false).size();
                    int random = new Random().nextInt(size);
                    int i = -1;
                    for (String treasure : file.get().getConfigurationSection("Treasures").getKeys(false)) {
                        if (++i != random) continue;

                        giveTreasure(player, treasure);
                        break;
                    } // else, we will create the respective chance of every treasure and run
                 */
                for (String treasure : file.get().getConfigurationSection("Treasures").getKeys(false)) {
                    double chance = file.get().getDouble("Treasures." + treasure + ".chance");
                    // If is a lucky block, chance will be double
                    if (luckyBlock) chance += chance;
                    // Lucky enchant, increasing chance...
                    if (new Random().nextInt(100 + 1) <= lucky && lucky > 0) chance += chance;
                    // Checking chance...
                    if (new Random().nextDouble() * 100D <= chance) {
                        giveTreasure(player, treasure);
                        break;
                    }
                }

                ActionBarAPI.sendActionBar(player, ChatColor.translateAlternateColorCodes('&',
                        StringUtils.replaceEach(file.get().getString("Messages.actionbar"), new String[]{
                                "{blocks}",
                                "{coins}",
                                "{tokens}"
                        }, new String[]{
                                NumberFormatter.formatNumber(blocks.doubleValue()),
                                NumberFormatter.formatNumber(coins.doubleValue()),
                                NumberFormatter.formatNumber(tokens.doubleValue())
                        })));

                Main.get().getPlayerCache().getCoinsCache().put(player, Main.get().getPlayerCache().getCoinsCache(player).add(coins));
                Main.get().getPlayerCache().getTokensCache().put(player, Main.get().getPlayerCache().getTokensCache(player).add(tokens));
                data.setBlocksAvaible(data.getBlocksAvaible().add(blocks));
                data.setBlocksBroken(data.getBlocksBroken().add(blocks));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock().getType().equals(Material.ANVIL)) {
                event.setCancelled(true);
                for (ProtectedRegion region : WGBukkit.getRegionManager(event.getClickedBlock().getWorld()).getApplicableRegions(event.getClickedBlock().getLocation())) {
                    if (getMineRegions().contains(region.getId())) {
                        if (player.getItemInHand().getType().toString().endsWith("_PICKAXE") && player.getItemInHand().getDurability() != 0) {
                            openRepairInventory(player);
                            player.playSound(player.getLocation(), Sound.ANVIL_LAND, 10, 10);
                        } else {
                            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                        }
                        return;
                    }
                }

                boolean repair = false;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null || item.getType().equals(Material.AIR) || item.getType().toString().endsWith("_PICKAXE")) continue;

                    if (item.getType().getMaxDurability() != 0 && item.getDurability() != 0) {
                        item.setDurability((short) 0);
                        repair = true;
                    }
                }

                for (ItemStack armor : player.getInventory().getArmorContents()) {
                    if (armor == null || armor.getType().equals(Material.AIR)) continue;

                    if (armor.getType().getMaxDurability() != 0 && armor.getDurability() != 0) {
                        armor.setDurability((short) 0);
                        repair = true;
                    }
                }

                if (repair) {
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 1);
                }
            }

            if (file.get().getString("Upgrade-Location") != null && deserializeLocation(file.get().getString("Upgrade-Location")).equals(event.getClickedBlock().getLocation())) {
                if (event.getItem() == null || event.getItem().getType().equals(Material.AIR) || !event.getItem().getType().toString().endsWith("_PICKAXE")) return;

                openUpgradeInventory(player);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 10, 10);
            }

            if (file.get().getString("Trade-Location") != null && deserializeLocation(file.get().getString("Trade-Location")).equals(event.getClickedBlock().getLocation())) {
                openTradeInventory(player);
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 10, 10);
            }

        }

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;

            NBTItem nbt = new NBTItem(event.getItem());
            if (!nbt.hasNBTData()) return;

            if (nbt.hasKey("tokens_amount")) {
                event.setCancelled(true);
                ItemStack item = event.getItem().clone();
                PlayerData data = Main.get().getDataManager().loadPlayer(player);
                if (!player.isSneaking()) {
                    item.setAmount(1);
                    data.setTokens(data.getTokens().add(new BigInteger(String.format("%.0f", nbt.getDouble("tokens_amount")))));
                } else {
                    for (int i = 0; i < item.getAmount(); ++i) {
                        data.setTokens(data.getTokens().add(new BigInteger(String.format("%.0f", nbt.getDouble("tokens_amount")))));
                    }
                }
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
                player.getInventory().removeItem(item);
            }

            else if (nbt.hasKey("blocks_amount")) {
                event.setCancelled(true);
                ItemStack item = event.getItem().clone();
                PlayerData data = Main.get().getDataManager().loadPlayer(player);
                if (!player.isSneaking()) {
                    item.setAmount(1);
                    data.setBlocksAvaible(data.getBlocksAvaible().add(new BigInteger(String.format("%.0f", nbt.getDouble("blocks_amount")))));
                } else {
                    for (int i = 0; i < item.getAmount(); ++i) {
                        data.setBlocksAvaible(data.getBlocksAvaible().add(new BigInteger(String.format("%.0f", nbt.getDouble("blocks_amount")))));
                    }
                }
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
                player.getInventory().removeItem(item);
            }

            else if (nbt.hasKey("mining_booster")) {
                event.setUseItemInHand(Event.Result.DENY);

                String[] split = nbt.getString("mining_booster").split("#");
                String type = split[0];

                if (Main.get().getPlayerBoosterManager().hasBoosterActived(player, type)) {
                    player.sendMessage(getColored(file.get().getString("Messages.has-booster-actived")));
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                    player.updateInventory();
                    return;
                }

                ItemStack item = event.getItem().clone();
                item.setAmount(1);

                int multiplier = Integer.parseInt(split[1]);
                int duration = Integer.parseInt(split[2]);

                Main.get().getPlayerBoosterManager().setBooster(player, type, multiplier, duration);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
                player.getInventory().removeItem(item);
            }
        }
    }

    private void openTradeInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, file.get().getInt("Inventories.trade.size"), ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories.trade.title")));
        PlayerData data = Main.get().getDataManager().loadPlayer(player);

        for (String str : file.get().getConfigurationSection("Inventories.trade.items").getKeys(false)) {
            String action = file.get().getString("Inventories.trade.items." + str + ".action", "NULL");
            int slot = file.get().getInt("Inventories.trade.items." + str + ".slot");
            double price = file.get().getDouble("Inventories.trade.items." + str + ".price", 0);
            ItemStack item = ItemBuilder.build(file, "Inventories.trade.items." + str, new String[]{
                    "{price}",
                    "{player}",
                    "{avaible}",
                    "{broken}"
            }, new String[]{
                    NumberFormatter.formatNumber(price),
                    player.getName(),
                    NumberFormatter.formatNumber(data.getBlocksAvaible().doubleValue()),
                    NumberFormatter.formatNumber(data.getBlocksBroken().doubleValue())
            });

            inventory.setItem(slot, item);

            if (!StringUtils.equals(action, "NULL")) {
                Main.get().getItemUtils().setItemAction(inventory, slot, () -> {
                    switch (action) {
                        case "BUY":
                            if (data.getBlocksAvaible().doubleValue() < price) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                                return;
                            }

                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
                            data.setBlocksAvaible(data.getBlocksAvaible().subtract(new BigInteger(String.format("%.0f", price))));

                            for (String cmd : file.get().getStringList("Inventories.trade.items." + str + ".commands")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                                    "{player}"
                                }, new String[]{
                                        player.getName()
                                }));
                            }
                            break;
                        case "CLOSE":
                            player.closeInventory();
                            break;
                    }
                });
            }
        }

        player.openInventory(inventory);
    }

    private void openUpgradeInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, file.get().getInt("Inventories.upgrade.size"), ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories.upgrade.title")));
        ItemStack pickaxe = player.getItemInHand();
        PickaxeUtils pickaxeUtils = new PickaxeUtils(pickaxe);

        for (String str : file.get().getConfigurationSection("Inventories.upgrade.items").getKeys(false)) {
            String action = file.get().getString("Inventories.upgrade.items." + str + ".action", "NULL");
            int slot = file.get().getInt("Inventories.upgrade.items." + str + ".slot");

            if (StringUtils.equalsIgnoreCase(str, "PICKAXE")) {
                inventory.setItem(slot, pickaxe);
                continue;
            }

            ItemStack item = null;

            if (StringUtils.equalsIgnoreCase(action, "TOP") || StringUtils.equalsIgnoreCase(action, "CLOSE") || StringUtils.contains(str.toUpperCase(), "UNKNOWN")) {
                item = ItemBuilder.build(file, "Inventories.upgrade.items." + str);
            } else {
                int enchantLevel = pickaxeUtils.getEnchantLevel(pickaxeUtils.getEnchant(str.toUpperCase()));
                int maxLevel = pickaxeUtils.getEnchant(str.toUpperCase()).getMaxLevel();
                BigInteger price = pickaxeUtils.getEnchant(str.toUpperCase()).getPricePerLevel();
                item = ItemBuilder.build(file, "Inventories.upgrade.items." + str, new String[]{
                        "{level}",
                        "{price}",
                        "{max_level}"
                }, new String[]{
                        NumberFormatter.fixDecimal(enchantLevel),
                        NumberFormatter.formatNumber(enchantLevel > 0 ? price.multiply(BigInteger.valueOf(enchantLevel)).doubleValue() : price.doubleValue()),
                        NumberFormatter.fixDecimal(maxLevel)
                });
            }

            inventory.setItem(slot, item);

            if (!StringUtils.equals(action, "NULL")) {
                Main.get().getItemUtils().setItemAction(inventory, slot, () -> {
                    switch (action) {
                        case "UPGRADE":
                            PlayerChatListener.getPlayerChat().put(player, new PlayerChat(player, pickaxe, Main.get().getEnchantsCache().getEnchant(str.toUpperCase())));
                            for (int i = 0; i < 25; ++i) {
                                player.sendMessage("");
                            }
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', file.get().getString("Messages.upgrade-type")));
                            player.closeInventory();
                            break;
                        case "TOP":
                            openTopTenInventory(player);
                            break;
                        case "CLOSE":
                            player.closeInventory();
                            break;
                        case "MAIN":
                            openUpgradeInventory(player);
                            break;
                    }
                });
            }
        }

        player.openInventory(inventory);
    }

    private void openTopTenInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, file.get().getInt("Inventories.top.size"), ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories.top.title")));

        String[] slots = file.get().getString("Inventories.top.slots").split(",");
        int pos = 0;

        for (PlayerData data : Main.get().getDataManager().getTopTenCache()) {
            inventory.setItem(Integer.parseInt(slots[pos]), ItemBuilder.build(file, "Inventories.top.items.player", new String[]{
                    "{player}",
                    "{pos}",
                    "{broken}",
                    "{avaible}"
            }, new String[]{
                    Bukkit.getOfflinePlayer(data.getUUID()).getName(),
                    String.valueOf(++pos),
                    NumberFormatter.formatNumber(data.getBlocksBroken().doubleValue()),
                    NumberFormatter.formatNumber(data.getBlocksAvaible().doubleValue())
            }));
        }

        for (String str : file.get().getConfigurationSection("Inventories.top.items").getKeys(false)) {
            if (StringUtils.equalsIgnoreCase(str, "PLAYER")) continue;

            ItemStack item = ItemBuilder.build(file, "Inventories.top.items." + str);
            String action = file.get().getString("Inventories.top.items." + str + ".action", "NULL");
            int slot = file.get().getInt("Inventories.top.items." + str + ".slot");
            inventory.setItem(slot, item);

            if (!StringUtils.equals(action, "NULL")) {
                Main.get().getItemUtils().setItemAction(inventory, slot, () -> {
                    switch (action) {
                        case "MAIN":
                            openUpgradeInventory(player);
                            break;
                    }
                });
            }
        }
        player.openInventory(inventory);
    }

    private void openRepairInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, file.get().getInt("Inventories.repair.size"), ChatColor.translateAlternateColorCodes('&', file.get().getString("Inventories.repair.title")));
        ItemStack pickaxe = player.getItemInHand();
        PlayerData data = Main.get().getDataManager().loadPlayer(player);

        for (String str : file.get().getConfigurationSection("Inventories.repair.items").getKeys(false)) {
            if (StringUtils.equalsIgnoreCase(str, "PICKAXE")) {
                int slot = file.get().getInt("Inventories.repair.items." + str + ".slot");
                inventory.setItem(slot, pickaxe);
                continue;
            }

            long price = file.get().getLong("Settings.price-per-repair") * pickaxe.getDurability();
            ItemStack item = ItemBuilder.build(file, "Inventories.repair.items." + str, new String[]{
                    "{percentage}",
                    "{price}"
            }, new String[]{
                    String.valueOf(pickaxe.getDurability() * 100 / pickaxe.getType().getMaxDurability()),
                    NumberFormatter.formatNumber(price)
            });
            int slot = file.get().getInt("Inventories.repair.items." + str + ".slot");
            String action = file.get().getString("Inventories.repair.items." + str + ".action", "NULL");

            inventory.setItem(slot, item);

            if (!StringUtils.equals(action, "NULL")) {
                Main.get().getItemUtils().setItemAction(inventory, slot, () -> {
                    switch (action) {
                        case "REPAIR":
                            if (data.getTokens().compareTo(BigInteger.valueOf(price)) >= 0) {
                                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 10);
                                player.getItemInHand().setDurability((short) 0);
                                player.closeInventory();
                                data.setTokens(data.getTokens().subtract(BigInteger.valueOf(price)));
                            } else {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                            }
                            break;
                    }
                });
            }
        }

        player.openInventory(inventory);
    }

    private void giveTreasure(Player player, String treasure) {
        List<String> commands = file.get().getStringList("Treasures." + treasure + ".commands");
        String randomStr = file.get().getString("Treasures." + treasure + ".random", "NULL");
        int min = 0;
        int max = 0;
        if (!StringUtils.equals(randomStr, "NULL")) {
            min = Integer.parseInt(file.get().getString("Treasures." + treasure + ".random").split(",")[0]);
            max = Integer.parseInt(file.get().getString("Treasures." + treasure + ".random").split(",")[1]);
        }
        int random = new Random().nextInt(max + 1 - min) + min;
        for (String cmd : commands) {
            Main.get().getServer().dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(cmd, player, random));
            // Treasures Booster
            if (Main.get().getPlayerBoosterManager().hasBoosterActived(player, "TREASURES")) {
                for (int i = 1; i < Main.get().getPlayerBoosterManager().getPlayerBooster(player, "TREASURES").getMultiplier(); ++i) {
                    Main.get().getServer().dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(cmd, player, random));
                }
            }
        }

        String title = getColored(replacePlaceholders(file.get().getString("Treasures." + treasure + ".title"), player, random));
        String subtitle = getColored(replacePlaceholders(file.get().getString("Treasures." + treasure + ".subtitle"), player, random));
        player.sendTitle(title, subtitle);
    }

    private String replacePlaceholders(String str, Player player, int random) {
        return StringUtils.replaceEach(str, new String[]{
                "{player}",
                "{random}"
        }, new String[]{
                player.getName(),
                String.valueOf(random)
        });
    }

    private String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private Location deserializeLocation(String str) {
        String[] split = str.split("#");
        return new Location(
                Bukkit.getWorld(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3])
        );
    }

    private List<String> getMineRegions() {
        return mineRegions;
    }

    private List<String> getMiningRegions() {
        return miningRegions;
    }

    private List<String> getBlacklistedRegions() {
        return blacklistedRegions;
    }
}