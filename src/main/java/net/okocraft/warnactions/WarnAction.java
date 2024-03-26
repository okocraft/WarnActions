package net.okocraft.warnactions;

import space.arim.libertybans.api.PunishmentType;

public sealed interface WarnAction permits WarnAction.AdditionalPunishment, WarnAction.ConsoleCommand {

    record AdditionalPunishment(PunishmentType type, String duration, String reason) implements WarnAction {
    }

    record ConsoleCommand(String commandline) implements WarnAction {
    }
}
