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
package net.nifheim.beelzebu.coins.bukkit.utils;

import java.io.File;
import java.util.List;
import net.nifheim.beelzebu.coins.core.Core;
import net.nifheim.beelzebu.coins.core.utils.MessagesManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Beelzebu
 */
public class Messages extends MessagesManager {

    private final Core core;
    private final String _lang;
    private File langFile;
    private FileConfiguration messages;

    public Messages(String lang) {
        super(lang);
        _lang = lang;
        core = Core.getInstance();
        langFile = new File(core.getDataFolder(), "messages" + _lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(core.getDataFolder(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(langFile);
    }

    @Override
    public Object get(String path) {
        return messages.get(path);
    }

    @Override
    public String getString(String path) {
        return messages.getString(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return messages.getStringList(path);
    }

    @Override
    public Boolean getBoolean(String path) {
        return messages.getBoolean(path);
    }

    @Override
    public Integer getInt(String path) {
        return messages.getInt(path);
    }

    @Override
    public Double getDouble(String path) {
        return messages.getDouble(path);
    }

    @Override
    public Object get(String path, Object def) {
        return (messages.get(path) == null ? def : messages.get(path));
    }

    @Override
    public String getString(String path, String def) {
        return (messages.get(path) == null ? def : messages.getString(path));
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return (messages.get(path) == null ? def : messages.getStringList(path));
    }

    @Override
    public Boolean getBoolean(String path, boolean def) {
        return (messages.get(path) == null ? def : messages.getBoolean(path));
    }

    @Override
    public Integer getInt(String path, int def) {
        return (messages.get(path) == null ? def : messages.getInt(path));
    }

    @Override
    public Double getDouble(String path, double def) {
        return (messages.get(path) == null ? def : messages.getDouble(path));
    }

    @Override
    public void set(String path, Object value) {
        messages.set(path, value);
    }

    @Override
    public Object getConfigurationSection(String path) {
        return messages.getConfigurationSection(path);
    }
}
