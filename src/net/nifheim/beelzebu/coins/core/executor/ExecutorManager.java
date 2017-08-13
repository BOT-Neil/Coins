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
package net.nifheim.beelzebu.coins.core.executor;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Beelzebu
 */
public class ExecutorManager {

    private static final Set<Executor> executors = new HashSet<>();

    public ExecutorManager() {
    }

    public void addExecutor(Executor ex) {
        executors.add(ex);
    }

    public Set<Executor> getExecutors() {
        return executors;
    }

    public Executor getExecutor(String id) {
        for (Executor ex : executors) {
            if (ex.getID().equals(id)) {
                return ex;
            }
        }
        return null;
    }
}
