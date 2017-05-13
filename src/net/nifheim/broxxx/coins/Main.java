package net.nifheim.broxxx.coins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.nifheim.broxxx.coins.command.CoinsCommand;
import net.nifheim.broxxx.coins.databasehandler.FlatFile;

import net.nifheim.broxxx.coins.listener.PlayerJoinListener;
import net.nifheim.broxxx.coins.listener.CommandListener;

import net.nifheim.broxxx.coins.hooks.MVdWPlaceholderAPIHook;
import net.nifheim.broxxx.coins.hooks.PlaceholderAPI;

import net.nifheim.broxxx.coins.databasehandler.MySQL;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin {

    private final File messagesFile = new File(getDataFolder(), "messages.yml");
    private FileConfiguration messages;

    public static String rep;
    public final ConsoleCommandSender console = Bukkit.getConsoleSender();

    private PlaceholderAPI placeholderAPI;

    private static Main instance;
    public static MySQL mysql;
    public static FlatFile ff;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        this.copyFiles();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public void onEnable() {

        instance = this;

        loadManagers();
        reloadConfig();

        getCommand("coins").setExecutor(new CoinsCommand());
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CommandListener(), this);

        if (getConfig().getBoolean("MySQL.Use")) {
            mysql = new MySQL();
            mysql.SQLConnection();
        } else {
            ff = new FlatFile(this);
        }

        updateMessages();

        this.motd();

        for (Player p : Bukkit.getOnlinePlayers()) {
            CoinsAPI.createPlayer(p);
        }
    }

    @Override
    public void onDisable() {

        this.reloadMessages();

        Bukkit.getScheduler().cancelTasks(this);

        this.motd();
    }

    private void copyFiles() {
        getDataFolder().mkdirs();

        if (!messagesFile.exists()) {
            copy(getResource("messages.yml"), messagesFile);
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            copy(getResource("config.yml"), configFile);
        }
    }

    private void loadManagers() {
        // Hook placeholders
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            console.sendMessage(rep("&8[&cCoins&8] &7MVdWPlaceholderAPI found, hooking in this."));
            MVdWPlaceholderAPIHook.hook(this);
        }
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            console.sendMessage(rep("&8[&cCoins&8] &7PlaceholderAPI found, hooking in this."));
            placeholderAPI = new PlaceholderAPI(this);
            placeholderAPI.hook();
        }
    }

    // SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public FileConfiguration getMessages() {
        return messages;
    }

    public String rep(String str) {
        return str
                .replaceAll("%prefix%", getMessages().getString("Prefix"))
                .replaceAll("&", "§");
    }

    public void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Can't copy the file " + file.getName() + " to the plugin data folder.", e.getCause());
        }
    }

    private void motd() {
        if (this.getDescription().getVersion().contains("BETA")) {
            console.sendMessage(rep(""));
            console.sendMessage(rep("    §c+=======================+"));
            console.sendMessage(rep("    §c|   §4Coins §fBy: §7Broxxx§c    |"));
            console.sendMessage(rep("    §c|-----------------------|"));
            console.sendMessage(rep("    §c|     §4v:§f" + getDescription().getVersion() + "      §c|"));
            console.sendMessage(rep("    §c+=======================+"));
            console.sendMessage(rep(""));
        } else {
            console.sendMessage(rep(""));
            console.sendMessage(rep("    §c+==================+"));
            console.sendMessage(rep("    §c| §4Coins §fBy: §7Broxxx§c |"));
            console.sendMessage(rep("    §c|------------------|"));
            console.sendMessage(rep("    §c|     §4v:§f" + getDescription().getVersion() + "      §c|"));
            console.sendMessage(rep("    §c+==================+"));
            console.sendMessage(rep(""));
        }
    }

    private void reloadMessages() {
        try {
            messages.load(messagesFile);
            messages.save(messagesFile);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateMessages() {
        if (getMessages().getInt("version") == 1) {
            getMessages().set("Errors.No Execute", "%prefix% &cCan't find a command to execute with this id.");
            getMessages().set("version", 2);
            try {
                messages.save(messagesFile);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
