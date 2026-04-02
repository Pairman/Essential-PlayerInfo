package org.eu.pnxlr.git.crosschat;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import org.eu.pnxlr.git.crosschat.chat.ChatRelayModule;
import org.eu.pnxlr.git.crosschat.tablist.GlobalTabListModule;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

@Plugin(
        id = "crosschat",
        name = "CrossChat",
        version = BuildConstants.VERSION,
        authors = {"Team-Jackdaw", "Pairman"}
)
public class CrossChat {

    private static final long TAB_LIST_REFRESH_INTERVAL_MILLIS = 1000L;

    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    public CrossChat(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Debug example: logger.info("Proxy online player count: {}", proxyServer.getPlayerCount());

        GlobalTabListModule tabListModule = new GlobalTabListModule(proxyServer);
        registerListener(tabListModule, "Registered global tab list module.");
        proxyServer.getScheduler()
                .buildTask(this, tabListModule::refreshLatencies)
                .repeat(TAB_LIST_REFRESH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .schedule();

        registerListener(new ChatRelayModule(proxyServer), "Registered cross-server chat relay.");
    }

    private void registerListener(Object listener, String logMessage) {
        proxyServer.getEventManager().register(this, listener);
        logger.info(logMessage);
    }
}
