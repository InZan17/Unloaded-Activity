package com.github.inzan17.config;

public class UnloadedActivityConfig {





    //General
    public int tickDifferenceThreshold = 100;
    public int maxNegativeBinomialAttempts = 20;
    public boolean debugLogs = false;





    //Chunk
    public int maxChunkUpdates = 8;
    public int maxKnownChunkUpdates = 64;
    public boolean randomizeBlockUpdates = false;
    public boolean rememberBlockPositions = true;
    public boolean multiplyMaxChunkUpdatesPerPlayer = false;
    public boolean updateAllChunksWhenSleep = true;
    public boolean enableRandomTicks = true;
    public boolean enablePrecipitationTicks = true;





    //Random Ticks
    public boolean growSaplings = true;
    public boolean growCrops = true;
    public boolean growStems = true;
    public boolean growSweetBerries = true;
    public boolean growCocoa = true;
    public boolean growSugarCane = true;
    public boolean growCactus = true;
    public boolean ageCopper = true;
    public boolean decayLeaves = true;
    public boolean growAmethyst = true;
    public boolean growGlowBerries = true;
    public boolean growKelp = true;
    public boolean growBamboo = true;
    public boolean hatchTurtleEggs = true;
    public boolean meltSnow = true;
    public boolean meltIce = true;
    public boolean growDripstone = true;
    public boolean dripstoneFillCauldrons = true;
    public boolean dripstoneTurnMudToClay = true;





    //Precipitation Ticks
    public boolean accumulateSnow = true;
    public boolean waterFreezing = true;
    public boolean weatherFillCauldron = true;





    //Accuracy
    public boolean accurateTurtleAgeAfterHatch = true;




    //Block Entities
    public boolean enableBlockEntities = true;
    public boolean updateFurnace = true;





    //Entities
    public boolean enableEntities = true;
    public boolean ageEntities = true;


}
