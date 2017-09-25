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
package net.nifheim.beelzebu.coins.core.multiplier;

/**
 *
 * @author Beelzebu
 */
public class MultiplierData {
    
    private final String server;
    private final String enabler;
    private final boolean enabled;
    private final int amount;
    private final int minutes;

    MultiplierData(String server, String enabler, boolean enabled, int amount, int minutes) {
        this.server = server;
        this.enabler = enabler;
        this.enabled = enabled;
        this.amount = amount;
        this.minutes = minutes;
    }
    
    public String getServer() {
        return server;
    }
    
    public String getEnabler() {
        return enabler;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getMinutes() {
        return minutes;
    }
}
