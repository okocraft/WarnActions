package net.okocraft.warnactions;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.decoder.Decoder;
import dev.siroshun.codec4j.api.decoder.object.ObjectDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record WarnActionConfig(
        Map<Integer, WarnAction.AdditionalPunishment> autoPunishments,
        Map<Integer, WarnAction.ConsoleCommand> autoCommands
) {

    public static Decoder<WarnActionConfig> DECODER = ObjectDecoder.create(
            WarnActionConfig::new,
            Codec.INT.toMapDecoderAsKey(WarnAction.AdditionalPunishment.DECODER).toOptionalFieldDecoder("auto-punishments", Map.of()),
            Codec.INT.toMapDecoderAsKey(WarnAction.ConsoleCommand.DECODER).toOptionalFieldDecoder("auto-commands", Map.of())
    );

    public Map<Integer, WarnAction[]> toMap() {
        var map = new HashMap<Integer, List<WarnAction>>();

        for (var entry : this.autoPunishments.entrySet()) {
            map.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }

        for (var entry : this.autoCommands.entrySet()) {
            map.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }

        return map.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().toArray(WarnAction[]::new)));
    }
}
