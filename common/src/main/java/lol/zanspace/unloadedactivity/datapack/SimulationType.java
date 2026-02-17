package lol.zanspace.unloadedactivity.datapack;

import java.util.Optional;

public enum SimulationType {
    INT_PROPERTY,
    ACTION;

    public static Optional<SimulationType> fromString(String string) {
        switch (string.toLowerCase()) {
            case "int_property" -> {
                return Optional.of(INT_PROPERTY);
            }
            case "action" -> {
                return Optional.of(ACTION);
            }
        }
        return Optional.empty();
    }
}
