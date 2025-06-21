package net.okocraft.warnactions;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.EnumCodec;
import dev.siroshun.codec4j.api.decoder.Decoder;
import dev.siroshun.codec4j.api.decoder.object.ObjectDecoder;
import space.arim.libertybans.api.PunishmentType;

public sealed interface WarnAction permits WarnAction.AdditionalPunishment, WarnAction.ConsoleCommand {

    record AdditionalPunishment(PunishmentType type, String duration, String reason) implements WarnAction {

        public static Decoder<AdditionalPunishment> DECODER = ObjectDecoder.create(
                AdditionalPunishment::new,
                EnumCodec.byName(PunishmentType.class).toRequiredFieldDecoder("type"),
                Codec.STRING.toOptionalFieldDecoder("duration", ""),
                Codec.STRING.toRequiredFieldDecoder("reason")
        );

    }

    record ConsoleCommand(String commandline) implements WarnAction {

        public static Decoder<ConsoleCommand> DECODER = ObjectDecoder.create(
                ConsoleCommand::new,
                Codec.STRING.toRequiredFieldDecoder("commandline")
        );

    }
}
