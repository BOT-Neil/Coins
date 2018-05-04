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
package io.github.beelzebu.coins.bungee.messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.beelzebu.coins.common.executor.ExecutorManager;
import io.github.beelzebu.coins.common.messaging.BungeeMessaging;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 *
 * @author Beelzebu
 */
public final class BungeeBungeeMessaging extends BungeeMessaging {

    @Override
    public void publishUser(UUID uuid, double coins) {
        ProxyServer.getInstance().getServers().forEach((id, server) -> sendMessage("Coins", "Update", Collections.singletonList("{\"uuid\":" + uuid + ",\"coins\":" + coins + "}"), false, server));
    }

    @Override
    public void getExecutors() {
        ExecutorManager.getExecutors().forEach(ex -> sendMessage("Coins", "Executors", Collections.singletonList(ex.toJson()), true));
    }

    @Override
    protected void sendMessage(String channel, String subchannel, List<String> messages, boolean wait) {
        sendMessage(channel, subchannel, messages, wait, ProxyServer.getInstance().getServers().entrySet().stream().findFirst().get().getValue());
    }

    private void sendMessage(String channel, String subchannel, List<String> messages, boolean wait, ServerInfo server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel);
        messages.forEach(msg -> out.writeUTF(msg));
        server.sendData(channel, out.toByteArray(), wait);
    }
}