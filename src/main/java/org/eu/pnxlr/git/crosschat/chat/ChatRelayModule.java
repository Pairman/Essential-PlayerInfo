package org.eu.pnxlr.git.crosschat.chat;

import org.eu.pnxlr.git.crosschat.text.MiniMessageHelper;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

/**
 * Relays player chat messages to players connected to other backend servers.
 */
public final class ChatRelayModule {

    private static final String MESSAGE_FORMAT = "<gray>[%server%] <%player%> %message%</gray>";

    private final ProxyServer proxyServer;

    public ChatRelayModule(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(currentServer -> {
            String formattedMessage = MESSAGE_FORMAT
                    .replace("%player%", player.getUsername())
                    .replace("%server%", currentServer.getServerInfo().getName())
                    .replace("%message%", event.getMessage());

            var component = MiniMessageHelper.deserialize(formattedMessage);
            for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
                if (!registeredServer.equals(currentServer.getServer())) {
                    registeredServer.sendMessage(component);
                }
            }
        });
    }
}
