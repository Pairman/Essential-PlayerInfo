package org.eu.pnxlr.git.crosschat.tablist;

import org.eu.pnxlr.git.crosschat.text.MiniMessageHelper;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;

/**
 * Shows remote players in the tab list for each connected player.
 */
public final class GlobalTabListModule {

    private static final String ENTRY_FORMAT = "[%server%] %player%";
    private static final int GAME_MODE = 3;

    private final ProxyServer proxyServer;

    public GlobalTabListModule(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        synchronizeEntries();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player disconnectedPlayer = event.getPlayer();
        for (Player viewer : proxyServer.getAllPlayers()) {
            viewer.getTabList().removeEntry(disconnectedPlayer.getUniqueId());
        }
    }

    /**
     * Updates remote player latency for already-visible tab list entries.
     */
    public void refreshLatencies() {
        for (Player viewer : proxyServer.getAllPlayers()) {
            for (Player target : proxyServer.getAllPlayers()) {
                if (!shouldDisplayRemotePlayer(viewer, target)) {
                    continue;
                }

                TabListEntry entry = viewer.getTabList().getEntry(target.getUniqueId()).orElse(null);
                if (entry == null) {
                    addOrReplaceEntry(viewer, target);
                    continue;
                }

                entry.setLatency((int) target.getPing());
            }
        }
    }

    private void synchronizeEntries() {
        for (Player viewer : proxyServer.getAllPlayers()) {
            for (Player target : proxyServer.getAllPlayers()) {
                if (shouldDisplayRemotePlayer(viewer, target)) {
                    addOrReplaceEntry(viewer, target);
                } else {
                    viewer.getTabList().removeEntry(target.getUniqueId());
                }
            }
        }
    }

    private boolean shouldDisplayRemotePlayer(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return false;
        }
        if (viewer.getCurrentServer().isEmpty() || target.getCurrentServer().isEmpty()) {
            return false;
        }

        String viewerServerName = viewer.getCurrentServer().get().getServerInfo().getName();
        String targetServerName = target.getCurrentServer().get().getServerInfo().getName();
        return !viewerServerName.equals(targetServerName);
    }

    private void addOrReplaceEntry(Player viewer, Player target) {
        TabList tabList = viewer.getTabList();
        tabList.removeEntry(target.getUniqueId());
        tabList.addEntry(TabListEntry.builder()
                .tabList(tabList)
                .profile(target.getGameProfile())
                .displayName(formatDisplayName(target))
                .latency((int) target.getPing())
                .gameMode(GAME_MODE)
                .build());
    }

    private Component formatDisplayName(Player player) {
        String serverName = player.getCurrentServer()
                .orElseThrow()
                .getServerInfo()
                .getName();
        String formattedEntry = ENTRY_FORMAT
                .replace("%player%", player.getUsername())
                .replace("%server%", serverName);
        return MiniMessageHelper.deserialize(formattedEntry);
    }
}
