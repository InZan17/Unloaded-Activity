package lol.zanspace.unloadedactivity.datapack;

import java.util.Optional;

public enum PropertyType {
    BOOL,
    INT;

    public static Optional<PropertyType> fromString(String string) {
        switch (string.toLowerCase()) {
            case "int" -> {
                return Optional.of(INT);
            }
            case "bool" -> {
                return Optional.of(BOOL);
            }
        }
        return Optional.empty();
    }
}
