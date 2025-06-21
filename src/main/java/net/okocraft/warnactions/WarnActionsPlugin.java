package net.okocraft.warnactions;

import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.siroshun.codec4j.api.error.DecodeError;
import dev.siroshun.codec4j.io.yaml.YamlIO;
import dev.siroshun.jfun.result.Result;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.event.PostPunishEvent;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.ListenerPriorities;
import space.arim.omnibus.events.RegisteredListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WarnActionsPlugin {

    private final List<RegisteredListener> registeredListeners = new ArrayList<>();

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    private LibertyBans libertyBans;

    @Inject
    public WarnActionsPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent ignored) {
        this.getProxy().getCommandManager().register("wareload", new ReloadCommand());

        this.getLogger().info("Loading config.yml...");

        Map<Integer, WarnAction[]> warnActionMap;

        try {
            warnActionMap = this.readWarnActions().toMap();
        } catch (IOException e) {
            this.getLogger().error("Could not load config.yml", e);
            return;
        }

        this.getLogger().info("Loading LibertyBans API...");
        this.libertyBans = OmnibusProvider.getOmnibus().getRegistry().getProvider(LibertyBans.class).orElseThrow();

        this.getLogger().info("Registering listeners...");
        this.registerListeners(warnActionMap);

        this.getLogger().info("Successfully enabled!");
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent ignored) {
        this.getProxy().getCommandManager().unregister("pnreload");

        this.getLogger().info("Unregistering listeners...");
        this.unregisterListeners();

        this.getLogger().info("Successfully disabled!");
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public LibertyBans getLibertyBans() {
        return this.libertyBans;
    }

    private void registerListeners(Map<Integer, WarnAction[]> warnActionmap) {
        this.registeredListeners.add(
                this.getLibertyBans().getOmnibus().getEventBus()
                        .registerListener(
                                PostPunishEvent.class,
                                ListenerPriorities.NORMAL,
                                new PunishmentListener(this, warnActionmap)
                        )
        );
    }

    private void unregisterListeners() {
        if (this.libertyBans != null) {
            this.registeredListeners.forEach(this.getLibertyBans().getOmnibus().getEventBus()::unregisterListener);
        }
    }

    private WarnActionConfig readWarnActions() throws IOException {
        var path = this.dataDirectory.resolve("config.yml");
        if (!Files.isRegularFile(path)) {
            try (var in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(Objects.requireNonNull(in), path);
            }
        }

        Result<WarnActionConfig, DecodeError> result = YamlIO.DEFAULT.decodeFrom(path, WarnActionConfig.CODEC);
        if (result.isFailure()) {
            throw new IOException(result.unwrapError().toString());
        }
        return result.unwrap();
    }

    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            var sender = invocation.source();

            WarnActionsPlugin.this.unregisterListeners();

            Map<Integer, WarnAction[]> warnActionMap;

            try {
                warnActionMap = WarnActionsPlugin.this.readWarnActions().toMap();
            } catch (IOException e) {
                WarnActionsPlugin.this.getLogger().error("Could not load config.yml", e);
                sender.sendMessage(Component.text("Could not reload config.yml. Please check the console."));
                return;
            }

            registerListeners(warnActionMap);

            sender.sendMessage(Component.text("WarnActions has been reloaded!"));
        }
    }
}
