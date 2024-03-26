package net.okocraft.warnactions;

import com.github.siroshun09.configapi.core.serialization.annotation.MapType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record WarnActionConfig(
        @MapType(key = Integer.class, value = WarnAction.AdditionalPunishment.class) Map<Integer, WarnAction.AdditionalPunishment> autoPunishment,
        @MapType(key = Integer.class, value = WarnAction.ConsoleCommand.class) Map<Integer, WarnAction.ConsoleCommand> autoCommand
) {

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
