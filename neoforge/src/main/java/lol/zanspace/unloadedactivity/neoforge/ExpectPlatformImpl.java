package lol.zanspace.unloadedactivity.neoforge;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
