package lol.zanspace.unloadedactivity.datapack;

import java.util.Optional;

public enum SimulationType {
    INT_PROPERTY,
    BUDDING,
    DECAY,
    ACTION;

    public static Optional<SimulationType> fromString(String string) {
        switch (string.toLowerCase()) {
            case "int_property" -> {
                return Optional.of(INT_PROPERTY);
            }
            case "budding" -> {
                return Optional.of(BUDDING);
            }
            case "decay" -> {
                return Optional.of(DECAY);
            }
            case "action" -> {
                return Optional.of(ACTION);
            }
        }
        return Optional.empty();
    }
}
