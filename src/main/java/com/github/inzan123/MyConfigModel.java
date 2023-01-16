package com.github.inzan123;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "unloadedactivity")
@Config(name = "unloaded-activity", wrapperName = "MyConfig")
public class MyConfigModel {
    public boolean debugLogs = false;
    public boolean growCrops = true;
    public boolean growStems = true;
}