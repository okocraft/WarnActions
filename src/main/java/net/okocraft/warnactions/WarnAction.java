package net.okocraft.warnactions;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.object.ObjectCodec;
import dev.siroshun.jfun.result.Result;
import space.arim.libertybans.api.PunishmentType;

public sealed interface WarnAction permits WarnAction.AdditionalPunishment, WarnAction.ConsoleCommand {

    record AdditionalPunishment(PunishmentType type, String duration, String reason) implements WarnAction {

        public static Codec<AdditionalPunishment> CODEC = ObjectCodec.create(
                AdditionalPunishment::new,
                Codec.STRING.flatXmap(
                        type -> Result.success(type.name()),
                        type -> {
                            try {
                                return Result.success(PunishmentType.valueOf(type));
                            } catch (IllegalArgumentException e) {
                                return Result.failure();
                            }
                        }
                ).toFieldCodec("type").required(AdditionalPunishment::type),
                Codec.STRING.toFieldCodec("duration").required(AdditionalPunishment::duration),
                Codec.STRING.toFieldCodec("reason").required(AdditionalPunishment::reason)
        );

    }

    record ConsoleCommand(String commandline) implements WarnAction {

        public static Codec<ConsoleCommand> CODEC = ObjectCodec.create(
                ConsoleCommand::new,
                Codec.STRING.toFieldCodec("commandline").required(ConsoleCommand::commandline)
        );

    }
}
