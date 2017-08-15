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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Beelzebu
 */
public class CacheManager {

    private static final Map<UUID, Double> data = Collections.synchronizedMap(new HashMap<>());

    public static Map<UUID, Double> getData() {
        return data;
    }

    public static void updateCoins(UUID uuid, Double coins) {
        if (data.containsKey(uuid)) {
            data.replace(uuid, coins);
        }
    }

    public void remove(UUID uuid) {
        if (data.containsKey(uuid)) {
            data.remove(uuid);
        }
    }
}
