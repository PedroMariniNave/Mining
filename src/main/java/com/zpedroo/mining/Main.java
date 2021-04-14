package com.zpedroo.mining;

import com.zpedroo.mining.cache.EnchantsCache;
import com.zpedroo.mining.cache.PlayerCache;
import com.zpedroo.mining.commands.*;
import com.zpedroo.mining.data.SQLiteConnector;
import com.zpedroo.mining.hooks.PlaceholderAPIHook;
import com.zpedroo.mining.hooks.VaultHook;
import com.zpedroo.mining.listeners.*;
import com.zpedroo.mining.managers.DataManager;
import com.zpedroo.mining.managers.FileManager;
import com.zpedroo.mining.cache.BlocksCache;
import com.zpedroo.mining.managers.PlayerBoosterManager;
import com.zpedroo.mining.utils.item.ItemUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Main extends JavaPlugin {

    private static Main main;
    public static Main get() { return main; }

    private HashMap<String, FileManager> files = new HashMap<>(1);
    private SQLiteConnector sqLiteConnector;
    private DataManager dataManager;
    private BlocksCache blocksCache;
    private PlayerCache playerCache;
    private EnchantsCache enchantsCache;
    private ItemUtils itemUtils;
    private PlayerBoosterManager playerBoosterManager;

    public void onEnable() {
        main = this;
        new PlaceholderAPIHook(this).register();
        new VaultHook().hook();
        getFiles().put("CONFIG", new FileManager("", "config", "configuration-files/config"));
        sqLiteConnector = new SQLiteConnector("mining");
        dataManager = new DataManager();
        blocksCache = new BlocksCache(getFiles().get("CONFIG"));
        playerCache = new PlayerCache(getFiles().get("CONFIG"));
        enchantsCache = new EnchantsCache(getFiles().get("CONFIG"));
        itemUtils = new ItemUtils();
        playerBoosterManager = new PlayerBoosterManager();
        registerCommands();
        registerListeners();
    }

    public void onDisable() {
        getDataManager().saveAll();
        getSQLiteConnector().closeConnection();
    }

    private void registerCommands() {
        getCommand("miningadmin").setExecutor(new MiningAdminCmd(getFiles().get("CONFIG")));
        getCommand("miningbooster").setExecutor(new MiningBoosterCmd(getFiles().get("CONFIG")));
        getCommand("tokens").setExecutor(new TokensCmd(getFiles().get("CONFIG")));
        getCommand("blocks").setExecutor(new BlocksCmd(getFiles().get("CONFIG")));
        getCommand("upgrader").setExecutor(new UpgraderCmd(getFiles().get("CONFIG")));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(getFiles().get("CONFIG")), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(getFiles().get("CONFIG")), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(getItemUtils(), getFiles().get("CONFIG")), this);
        getServer().getPluginManager().registerEvents(new PlayerPreventListeners(getFiles().get("CONFIG")), this);
        getServer().getPluginManager().registerEvents(new RegionListeners(getFiles().get("CONFIG")), this);
    }

    public HashMap<String, FileManager> getFiles() {
        return files;
    }

    public SQLiteConnector getSQLiteConnector() {
        return sqLiteConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public BlocksCache getBlocks() {
        return blocksCache;
    }

    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    public EnchantsCache getEnchantsCache() {
        return enchantsCache;
    }

    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    public PlayerBoosterManager getPlayerBoosterManager() {
        return playerBoosterManager;
    }
}
