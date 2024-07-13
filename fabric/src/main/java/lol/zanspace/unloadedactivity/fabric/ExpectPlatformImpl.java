package lol.zanspace.unloadedactivity.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
