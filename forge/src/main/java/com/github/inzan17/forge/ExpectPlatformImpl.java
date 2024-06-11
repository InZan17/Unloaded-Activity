package com.github.inzan17.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ExpectPlatformImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
