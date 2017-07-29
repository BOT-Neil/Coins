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
package net.nifheim.broxxx.coins;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import net.nifheim.broxxx.coins.executor.ExecutorManager;

/**
 *
 * @author Beelzebu
 */
public class BungeeMain extends Plugin implements Listener {

    private static BungeeMain plugin;

    public BungeeMain getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        ProxyServer.getInstance().registerChannel("Coins");
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onMessageReceive(PluginMessageEvent e) {
        if (e.getTag().equals("BungeeCord")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
            String channel = in.readUTF();
            if (channel.equals("Coins")) {
                ServerInfo server = ProxyServer.getInstance().getPlayer(e.getReceiver().toString()).getServer().getInfo();
                String input = in.readUTF(); // the inputstring
                if (input.equals("getexecutors")) {
                    sendToBukkit("Coins", "", server);
                } else if (input.equals("execute ")) {
                    String[] msg = input.split(" ");
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getPlayer(msg[1]), input.substring((msg[0] + msg[1]).length() + 2));
                }
            }
        }
    }

    public void sendToBukkit(String channel, String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ByteArrayDataOutput out = ByteStreams.newDataOutput(stream);
        out.writeUTF(channel);
        out.writeUTF(message);
        server.sendData("Return", stream.toByteArray());
    }

    public void execute(String executorid, ProxiedPlayer p) {
        ExecutorManager ex = new ExecutorManager();
        if (ex.getExecutor(executorid) != null) {
            /*
            if (CoinsAPI.getCoins(p.getName()) >= ex.getExecutor(executorid).getCost()) {
            }
            */
        }
    }
}
