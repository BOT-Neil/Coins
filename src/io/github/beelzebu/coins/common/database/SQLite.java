/**
 * This file is part of Coins
 *
 * Copyright © 2018 Beelzebu
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
package io.github.beelzebu.coins.common.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.beelzebu.coins.common.utils.FileManager;
import io.github.beelzebu.coins.common.utils.database.DatabaseUtils;
import io.github.beelzebu.coins.common.utils.database.SQLQuery;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Beelzebu
 */
public final class SQLite extends CoinsDatabase {

    @Override
    public void setup() {
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("Coins SQLite Connection Pool");
        hc.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        hc.setJdbcUrl("jdbc:sqlite:plugins/Coins/database.db");
        hc.setConnectionTestQuery("SELECT 1");
        hc.setMinimumIdle(1);
        hc.setConnectionTimeout(10000);
        hc.setMaximumPoolSize(4);
        hc.setLeakDetectionThreshold(30000);
        hc.validate();
        ds = new HikariDataSource(hc);
        updateDatabase();
    }

    @Override
    protected void updateDatabase() {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            String data
                    = "CREATE TABLE IF NOT EXISTS `" + DATA_TABLE + "`"
                    + "(`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "`uuid` VARCHAR(50),"
                    + "`nick` VARCHAR(50),"
                    + "`balance` DOUBLE,"
                    + "`lastlogin` LONG);";
            String multiplier = "CREATE TABLE IF NOT EXISTS `" + MULTIPLIERS_TABLE + "`"
                    + "(`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "`server` VARCHAR(50),"
                    + "`uuid` VARCHAR(50),"
                    + "`type` VARCHAR(20),"
                    + "`amount` INTEGER,"
                    + "`minutes` INTEGER,"
                    + "`endtime` LONG,"
                    + "`queue` INT,"
                    + "`enabled` BOOLEAN);";
            st.executeUpdate(data);
            st.executeUpdate(multiplier);
            if (CORE.getConfig().getInt("Database Version", 1) < 2) {
                try {
                    if (DriverManager.getConnection("jdbc:sqlite:plugins/Coins/database.old.db").prepareStatement("SELECT * FROM Data;").executeQuery().next() && !c.prepareStatement("SELECT * FROM " + DATA_TABLE + ";").executeQuery().next()) {
                        CORE.log("Seems that your database is outdated, we'll try to update it...");
                        ResultSet res = DriverManager.getConnection("jdbc:sqlite:plugins/Coins/database.old.db").prepareStatement("SELECT * FROM Data;").executeQuery();
                        while (res.next()) {
                            DatabaseUtils.prepareStatement(c, SQLQuery.CREATE_USER, res.getString("uuid"), res.getString("nick"), res.getDouble("balance"), res.getLong("lastlogin")).executeUpdate();
                            CORE.debug("Migrated the data for " + res.getString("nick") + " (" + res.getString("uuid") + ")");
                        }
                        CORE.log("Successfully upadated database to version 2");
                    }
                    new FileManager().updateDatabaseVersion(2);
                } catch (SQLException ex) {
                    for (int i = 0; i < 5; i++) {
                        CORE.log("An error has ocurred migrating the data from the old database, check the logs ASAP!");
                    }
                    CORE.debug(ex);
                    return;
                }
            }
            if (CORE.getConfig().getBoolean("General.Purge.Enabled", true) && CORE.getConfig().getInt("General.Purge.Days") > 0) {
                st.executeUpdate("DELETE FROM " + DATA_TABLE + " WHERE lastlogin < " + (System.currentTimeMillis() - (CORE.getConfig().getInt("General.Purge.Days", 60) * 86400000L)) + ";");
                CORE.debug("Inactive users were removed from the database.");
            }
        } catch (SQLException ex) {
            CORE.log("Something was wrong creating the default databases. Please check the debug log.");
            CORE.debug(ex);
        }
    }
}
