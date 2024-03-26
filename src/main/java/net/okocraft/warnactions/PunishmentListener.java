package net.okocraft.warnactions;

import space.arim.libertybans.api.PlayerVictim;
import space.arim.libertybans.api.PunishmentType;
import space.arim.libertybans.api.event.PostPunishEvent;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.omnibus.events.AsynchronousEventConsumer;
import space.arim.omnibus.events.EventFireController;

import java.time.Duration;
import java.util.Map;

public class PunishmentListener implements AsynchronousEventConsumer<PostPunishEvent> {

    private final Map<Integer, WarnAction[]> warnActionMap;
    private final WarnActionsPlugin plugin;

    public PunishmentListener(WarnActionsPlugin plugin, Map<Integer, WarnAction[]> warnActionMap) {
        this.plugin = plugin;
        this.warnActionMap = warnActionMap;
    }

    @Override
    public void acceptAndContinue(PostPunishEvent event, EventFireController controller) {
        var punishment = event.getPunishment();

        if (punishment.getType() != PunishmentType.WARN || !(punishment.getVictim() instanceof PlayerVictim player)) {
            controller.continueFire();
            return;
        }

        var count =
                this.plugin.getLibertyBans().getSelector().selectionBuilder()
                        .victim(punishment.getVictim())
                        .type(PunishmentType.WARN)
                        .selectActiveOnly()
                        .build()
                        .countNumberOfPunishments()
                        .toCompletableFuture().join();

        var actions = this.warnActionMap.get(count);
        this.plugin.getProxy().getScheduler().buildTask(this.plugin, () -> doActions(punishment, player, actions)).delay(Duration.ofSeconds(1)).schedule();
        controller.continueFire();
    }

    private void doActions(Punishment punishment, PlayerVictim player, WarnAction[] actions) {
        if (actions == null) {
            return;
        }

        for (var action : actions) {
            doAction(punishment, player, action);
        }
    }

    private void doAction(Punishment punishment, PlayerVictim player, WarnAction warnAction) {
        if (warnAction instanceof WarnAction.AdditionalPunishment additionalPunishment) {
            addPunishment(punishment, player, additionalPunishment);
        } else if (warnAction instanceof WarnAction.ConsoleCommand consoleCommand) {
            executeCommand(punishment, player, consoleCommand);
        }
    }

    private void addPunishment(Punishment punishment, PlayerVictim player, WarnAction.AdditionalPunishment additional) {
        if (additional.type() == PunishmentType.WARN ||
                (additional.type() == PunishmentType.KICK && this.plugin.getProxy().getPlayer(player.getUUID()).isEmpty())) {
            return;
        }

        var uuid = player.getUUID();
        var reason = additional.reason().replace("%REASON%", punishment.getReason());

        String command;
        if (additional.type() == PunishmentType.KICK) {
            command = "kick " + uuid + " " + reason;
        } else {
            this.plugin.getLibertyBans().getRevoker()
                    .revokeByTypeAndVictim(additional.type(), player)
                    .undoPunishment()
                    .toCompletableFuture().join();
            command = additional.type().toString() + " " + uuid + " " + additional.duration() + " " + reason;
        }

        this.plugin.getProxy().getCommandManager().executeAsync(this.plugin.getProxy().getConsoleCommandSource(), command).join();
    }

    private void executeCommand(Punishment punishment, PlayerVictim player, WarnAction.ConsoleCommand consoleCommand) {
        var playerName = this.plugin.getLibertyBans().getUserResolver().lookupName(player.getUUID()).join().orElse(null);

        if (playerName == null) {
            return;
        }

        this.plugin.getProxy().getCommandManager().executeAsync(
                this.plugin.getProxy().getConsoleCommandSource(),
                consoleCommand.commandline()
                        .replace("%TARGET%", playerName)
                        .replace("%REASON%", punishment.getReason())
        ).join();
    }
}
