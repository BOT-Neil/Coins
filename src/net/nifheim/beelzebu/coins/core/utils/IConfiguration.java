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
package net.nifheim.beelzebu.coins.core.utils;

import java.util.List;

/**
 *
 * @author Beelzebu
 */
public interface IConfiguration {
    
    Object get(String path);
    
    String getString(String path);
    
    List<String> getStringList(String path);
    
    Boolean getBoolean(String path);
    
    Integer getInt(String path);
    
    Double getDouble(String path);
    
    Object get(String path, Object def);
    
    String getString(String path, String def);
    
    List<String> getStringList(String path, List<String> def);
    
    Boolean getBoolean(String path, boolean def);
    
    Integer getInt(String path, int def);
    
    Double getDouble(String path, double def);
    
    void set(String path, Object value);
    
    Object getConfigurationSection(String path);
}
