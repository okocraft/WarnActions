package net.okocraft.warnactions;

import dev.siroshun.codec4j.api.codec.Codec;
import dev.siroshun.codec4j.api.codec.object.ObjectCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record WarnActionConfig(
        Map<Integer, WarnAction.AdditionalPunishment> autoPunishment,
        Map<Integer, WarnAction.ConsoleCommand> autoCommand
) {

    public static Codec<WarnActionConfig> CODEC = ObjectCodec.create(
            WarnActionConfig::new,
            Codec.INT.toMapCodecAsKey(WarnAction.AdditionalPunishment.CODEC).toFieldCodec("autoPunishment").defaultValue(Map.of()).optional(WarnActionConfig::autoPunishment),
            Codec.INT.toMapCodecAsKey(WarnAction.ConsoleCommand.CODEC).toFieldCodec("autoCommand").defaultValue(Map.of()).optional(WarnActionConfig::autoCommand)
    );

    public Map<Integer, WarnAction[]> toMap() {
        var map = new HashMap<Integer, List<WarnAction>>();

        for (var entry : this.autoPunishment.entrySet()) {
            map.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }

        for (var entry : this.autoCommand.entrySet()) {
            map.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }

        return map.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().toArray(WarnAction[]::new)));
    }
}
