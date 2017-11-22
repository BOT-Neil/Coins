/**
 * This file is part of Coins
 *
 * Copyright (C) 2017 Beelzebu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.nifheim.beelzebu.coins.bukkit;

import java.util.Iterator;
import java.util.UUID;
import net.nifheim.beelzebu.coins.CoinsAPI;
import net.nifheim.beelzebu.coins.bukkit.command.CommandManager;
import net.nifheim.beelzebu.coins.bukkit.listener.*;
import net.nifheim.beelzebu.coins.bukkit.utils.CoinsEconomy;
import net.nifheim.beelzebu.coins.bukkit.utils.Configuration;
import net.nifheim.beelzebu.coins.bukkit.utils.bungee.PluginMessage;
import net.nifheim.beelzebu.coins.bukkit.utils.placeholders.*;
import net.nifheim.beelzebu.coins.core.Core;
import net.nifheim.beelzebu.coins.core.executor.Executor;
import net.nifheim.beelzebu.coins.core.utils.CacheManager;
import net.nifheim.beelzebu.coins.core.utils.CoinsConfig;
import net.nifheim.beelzebu.coins.core.utils.dependencies.DependencyManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private final Core core = Core.getInstance();
    private static Main instance;
    private CommandManager commandManager;
    private Configuration configuration;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        core.setup(new BukkitMethods());
        DependencyManager.loadAllDependencies();
    }

    @Override
    public void onEnable() {
        configuration = new Configuration(this);
        core.start();
        commandManager = new CommandManager();
        loadManagers();
        startListeners();
        getConfig().getConfigurationSection("Command executor").getKeys(false).forEach((id) -> {
            core.getExecutorManager().addExecutor(new Executor(id, getConfig().getString("Command executor." + id + ".Displayname", id), getConfig().getDouble("Command executor." + id + ".Cost", 0), getConfig().getStringList("Command executor." + id + ".Command")));
        });
        startTasks();
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("Vault.Use", false)) {
            new CoinsEconomy(this).shutdown();
        }
        commandManager.unregisterCommand();
        Bukkit.getScheduler().cancelTasks(this);
        core.shutdown();
    }

    private void loadManagers() {
        // Create the command
        commandManager.registerCommand();
        // Hook placeholders
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            core.getMethods().log("PlaceholderAPI found, hooking in ");
            new CoinsPlaceholders(this).hook();
            new MultipliersPlaceholders(this).hook();
        }
        if (getConfig().getBoolean("Vault.Use", false)) {
            if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                new CoinsEconomy(this).setup();
            } else {
                core.log("You enabled Vault in the config, but the plugin Vault can't be found.");
            }
        }
    }

    private void startListeners() {
        Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new InternalListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);
    }

    private void startTasks() {
        PluginMessage pmsg = new PluginMessage();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "Coins");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "Coins", pmsg);
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            pmsg.sendToBungeeCord("Multiplier", "getAllMultipliers");
            pmsg.sendToBungeeCord("Coins", "getExecutors");
            Bukkit.getOnlinePlayers().forEach((p) -> {
                CoinsAPI.createPlayer(p.getName(), p.getUniqueId());
            });
        }, 30);
        core.debug("Starting cache cleanup task");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Iterator<UUID> it = CacheManager.getPlayersData().keySet().iterator(); it.hasNext(); ) {
                UUID uuid = it.next();
                if (!core.getMethods().isOnline(uuid)) {
		    it.remove();
                    core.debug("Removed '" + uuid + "' from the cache.");
                }
            }
        }, 100, 12000);
    }

    public CoinsConfig getConfiguration() {
        return configuration;
    }
}
