/*
 * This file is part of Coins.
 *
 * Copyright © 2017 Beelzebu
 * Coins is licensed under the GNU General Public License.
 *
 * Coins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coins is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.nifheim.broxxx.coins.databasehandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.nifheim.broxxx.coins.Main;
import net.nifheim.broxxx.coins.multiplier.MultiplierType;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author Beelzebu
 */
public class MySQL {

    private final Main plugin;

    private final String host;
    private final int port;
    private final String name;
    private final String user;
    private final String passwd;
    private final String prefix;
    private final int checkdb;
    private static Connection c;
    private String player;

    public MySQL(Main main) {
        plugin = main;
        host = plugin.getConfig().getString("MySQL.Host");
        port = plugin.getConfig().getInt("MySQL.Port");
        name = plugin.getConfig().getString("MySQL.Database");
        user = plugin.getConfig().getString("MySQL.User");
        passwd = plugin.getConfig().getString("MySQL.Password");
        prefix = plugin.getConfig().getString("MySQL.Prefix");
        checkdb = plugin.getConfig().getInt("MySQL.Connection Interval") * 1200;

        SQLConnection();
    }

    public static Connection getConnection() {
        return c;
    }

    private void SQLConnection() {
        Connect();

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            plugin.log("Checking the database connection ...");
            if (MySQL.getConnection() == null) {
                plugin.log("The database connection is null, reconnecting ...");
                Reconnect();
            } else {
                plugin.log("The connection to the database is still active.");
            }
        }, 0L, checkdb);
    }

    private void Connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            plugin.log("Database driver can''t be found, disabling plugin!");
            plugin.debug(e.toString());
        }
        try {
            c = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true", user, passwd);
            String createData
                    = "CREATE TABLE IF NOT EXISTS `" + prefix + "Data`"
                    + "(`uuid` VARCHAR(50) NOT NULL,"
                    + "`nick` VARCHAR(50) NOT NULL,"
                    + "`balance` DOUBLE NOT NULL,"
                    + "`lastlogin` LONG NOT NULL,"
                    + "PRIMARY KEY (`uuid`));";
            String createMultiplier
                    = "CREATE TABLE IF NOT EXISTS `" + prefix + "Multipliers`"
                    + "(`id` INT NOT NULL AUTO_INCREMENT,"
                    + "`uuid` VARCHAR(50) NOT NULL,"
                    + "`multiplier` INT,"
                    + "`queue` INT AUTO_INCREMENT,"
                    + "`minutes` INT,"
                    + "`starttime` LONG,"
                    + "`endtime` LONG,"
                    + "`server` VARCHAR(50),"
                    + "`enabled` BOOLEAN,"
                    + "PRIMARY KEY (`id`));";

            Statement update = c.createStatement();
            update.execute(createData);
            update.execute(createMultiplier);
            if (!MySQL.getConnection().isClosed()) {
                plugin.log("Plugin conected sucesful to the MySQL.");
            }
        } catch (SQLException ex) {
            plugin.debug(String.format("Something was wrong with the connection, the error code is: %s", ex.getErrorCode()));
            plugin.log("Can't connect to the database, disabling plugin...");
            Bukkit.getScheduler().cancelTasks(Main.getInstance());
            Bukkit.getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    private void Reconnect() {
        Disconnect();

        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            Connect();
        }, 20L);
    }

    private void Disconnect() {
        try {
            if (!c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            plugin.debug("Something was wrong disconnecting from the database, error code is: " + e.getErrorCode());
        }
    }

    private String player(OfflinePlayer p) {
        player = p.getUniqueId().toString();
        return player;
    }

    // ---------- DATABASE QUERIES ---------- //
    public Double getCoins(Player p) {
        try {
            String localplayer = player(p);

            ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");

            if (res.next() && res.getString("uuid") != null) {
                double coins = res.getDouble("balance");

                return coins;
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred creating the data for player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
        return 0D;
    }

    public Double getCoinsOffline(OfflinePlayer p) {
        try {
            String localplayer = player(p);

            ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");

            if (res.next() && res.getString("uuid") != null) {
                double coins = res.getDouble("balance");

                return coins;
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred creating the data for player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
        return 0D;
    }

    public void addCoins(Player p, double coins, boolean multiply) {
        getMultiplierTime(plugin.getConfig().getString("Multipliers.Server"));
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid ='" + localplayer + "';");

            if (res.next() && res.getString("uuid") != null) {
                if (multiply) {
                    coins = coins * plugin.getConfig().getInt("Multipliers.Amount");
                }
                double oldCoins = res.getDouble("balance");

                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + (oldCoins + coins) + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred adding coins to the player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void addCoinsOffline(OfflinePlayer p, double coins) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid ='" + localplayer + "';");

            if (res.next() && res.getString("uuid") != null) {
                double oldCoins = res.getDouble("balance");

                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + (oldCoins + coins) + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred adding coins to the player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void takeCoins(Player p, double coins) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();
            double beforeCoins = res.getDouble("balance");
            if (res.getString("uuid") != null) {

                if (beforeCoins - coins < 0) {
                    if (!plugin.getConfig().getBoolean("Allow Negative")) {
                        Statement update = c.createStatement();
                        update.executeUpdate("UPDATE " + prefix + "Data SET balance = 0 WHERE uuid = '" + localplayer + "';");
                    }
                } else if (beforeCoins == coins) {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET balance = 0 WHERE uuid = '" + localplayer + "';");
                } else if (beforeCoins > coins) {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + (beforeCoins - coins) + " WHERE uuid = '" + localplayer + "';");
                }
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred taking coins to the player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void takeCoinsOffline(OfflinePlayer p, double coins) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();

            if (res.getString("uuid") != null) {
                double beforeCoins = res.getDouble("balance");

                if (beforeCoins - coins < 0) {
                    if (!plugin.getConfig().getBoolean("Allow Negative")) {
                        return;
                    }
                    if (plugin.getConfig().getBoolean("Allow Negative")) {
                        Statement bypassUpdate = c.createStatement();
                        bypassUpdate.executeUpdate("UPDATE " + prefix + "Data SET balance = " + (beforeCoins - coins) + " WHERE uuid = '" + localplayer + "';");
                    }
                } else if (beforeCoins == coins) {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET balance = 0 WHERE uuid = '" + localplayer + "';");
                } else if (beforeCoins > coins) {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + (beforeCoins - coins) + " WHERE uuid = '" + localplayer + "';");
                }
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred taking coins to the player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void resetCoins(Player p) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();

            if (res.getString("uuid") != null) {
                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + plugin.getConfig().getInt("General.Starting Coins", 0) + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred reseting the coins of player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void resetCoinsOffline(OfflinePlayer p) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();

            if (res.getString("uuid") != null) {
                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + plugin.getConfig().getInt("General.Starting Coins", 0) + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred reseting the coins of player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void setCoins(Player p, double coins) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();

            if (res.getString("uuid") != null) {
                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + coins + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred setting the coins of player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public void setCoinsOffline(OfflinePlayer p, double coins) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            res.next();

            if (res.getString("uuid") != null) {
                Statement update = c.createStatement();
                update.executeUpdate("UPDATE " + prefix + "Data SET balance = " + coins + " WHERE uuid = '" + localplayer + "';");
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred setting the coins of player: " + p.getName());
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
    }

    public boolean isindb(UUID p) {
        try {
            String localplayer = p.toString();
            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
            if (res.next()) {
                return res.getString("uuid") != null;
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred cheking if the player: " + Bukkit.getPlayer(p).getName() + " exists in the database.");
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
        return false;
    }

    public boolean isindb(String playerName) {
        try {
            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data WHERE uuid = '" + playerName + "';");
            if (res.next()) {
                return res.getString("nick") != null;
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred cheking if the player: " + playerName + " exists in the database.");
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
            plugin.debug(ex.getMessage());
        }
        return false;
    }

    public List<String> getTop(int top) {
        List<String> toplist = new ArrayList<>();
        try {
            Statement check = c.createStatement();
            ResultSet res = check.executeQuery("SELECT * FROM " + prefix + "Data ORDER BY balance DESC LIMIT " + top + ";");
            while (res.next()) {
                String playername = res.getString("nick");
                toplist.add(playername);
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred generating the toplist");
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
        }
        return toplist;
    }

    public void createPlayer(Player p) {
        try {
            String localplayer = player(p);

            Statement check = c.createStatement();
            if (plugin.getConfig().getBoolean("Online Mode")) {
                ResultSet res = check.executeQuery("SELECT uuid FROM " + prefix + "Data WHERE uuid = '" + localplayer + "';");
                if (!res.next()) {
                    Statement update = c.createStatement();
                    update.executeUpdate("INSERT INTO " + prefix + "Data VALUES ('" + localplayer + "', '" + p.getName() + "', 0.0, " + System.currentTimeMillis() + ");");
                    plugin.debug("An entry in the database was created for: " + p.getName());
                } else {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET nick = '" + p.getName() + "', lastlogin = " + System.currentTimeMillis() + " WHERE uuid = '" + localplayer + "';");
                    plugin.debug("The nickname of: " + p.getName() + " was updated in the database.");
                }
            } else {
                ResultSet res = check.executeQuery("SELECT nick FROM " + prefix + "Data WHERE nick = '" + p.getName() + "';");
                if (!res.next()) {
                    Statement update = c.createStatement();
                    update.executeUpdate("INSERT INTO " + prefix + "Data VALUES ('" + localplayer + "', '" + p.getName() + "', 0.0, " + System.currentTimeMillis() + ");");
                    plugin.debug("An entry in the database was created for: " + p.getName());
                } else {
                    Statement update = c.createStatement();
                    update.executeUpdate("UPDATE " + prefix + "Data SET uuid = '" + localplayer + "', lastlogin = " + System.currentTimeMillis() + " WHERE nick = '" + p.getName() + "';");
                    plugin.debug("The uuid of: " + localplayer + " was updated in the database.");
                }
            }
        } catch (SQLException ex) {
            plugin.log("&cAn internal error has occurred creating the player: " + p.getName() + " in the database.");
            plugin.debug("&cThe error code is: " + ex.getErrorCode());
            plugin.debug(ex.getMessage());
        }
    }

    // ---------- MULTIPLIERS ---------- //
    public Long getMultiplierTime(String server) {
        try {
            ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Multipliers WHERE server = '" + server + "' AND enabled = true;");
            if (res.next()) {
                Long start = System.currentTimeMillis();
                Long end = res.getLong("endtime");
                if ((end - start) > 0) {
                    return (end - start);
                } else {
                    c.createStatement().executeUpdate("DELETE FROM " + prefix + "Multipliers WHERE server = '" + server + "' AND enabled = true;");
                    plugin.getConfig().set("Multipliers.Amount", 1);
                    plugin.saveConfig();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0L;
    }

    public void createMultiplier(Player p, Integer multiplier, Integer minutes) {
        try {
            String localplayer = player(p);
            c.createStatement().executeUpdate("INSERT INTO " + prefix + "Multipliers VALUES(NULL, '" + localplayer + "', " + multiplier + ", -1, " + minutes + ", 0, 0, " + "'" + plugin.getConfig().getString("Multipliers.Server") + "'" + ", false);");
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void useMultiplier(Player p, Integer id, String server, final MultiplierType type) {
        try {
            String localplayer = player(p);
            ResultSet res0 = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Multipliers");
            res0.next();
            Boolean enabled = res0.getBoolean("enabled");
            if (!enabled) {
                ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Multipliers WHERE id = " + id + " AND uuid = '" + localplayer + "';");
                if (res.next()) {
                    Long minutes = res.getLong("minutes");
                    Long endtime = System.currentTimeMillis() + (minutes * 60000);
                    switch (type) {
                        case GLOBAL:
                            server = type.toString();
                            break;
                        case PERSONAL:
                            server = type.toString();
                            break;
                        case SERVER:
                        default:
                            break;
                    }
                    c.createStatement().executeUpdate("UPDATE " + prefix + "Multipliers SET endtime = " + endtime + ", enabled = true, server = '" + server + "' WHERE id = " + id + ";");
                    plugin.getConfig().set("Multipliers.Amount", res.getInt("multiplier"));
                    plugin.saveConfig();
                } else {
                    p.sendMessage(plugin.getString("Multipliers.No Multipliers"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Set<Integer> getMultipliersFor(Player p, String server) {
        String localplayer = player(p);
        Set<Integer> multipliers = new HashSet<>();
        try {
            String query = "SELECT * FROM " + prefix + "Multipliers WHERE uuid = '" + localplayer + "' AND enabled = false";
            if (server != null) {
                query += " AND server = '" + server + "'";
            }
            ResultSet res = c.createStatement().executeQuery(query + ";");
            while (res.next()) {
                multipliers.add(res.getInt("id"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return multipliers;
    }

    public Player getEnabler(String server) {
        Player enabler = null;
        try {
            ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Multipliers WHERE enabled = true AND server = '" + server + "';");
            if (res.next()) {
                enabler = Bukkit.getPlayer(UUID.fromString(res.getString("uuid")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return enabler;
    }

    public int getMultiplierAmount(String server) {
        int amount = 1;
        try {
            ResultSet res = c.createStatement().executeQuery("SELECT * FROM " + prefix + "Multipliers WHERE enabled = true AND server = '" + server + "';");
            if (res.next()) {
                amount = res.getInt("multiplier");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return amount;
    }
}
