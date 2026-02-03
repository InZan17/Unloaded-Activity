package lol.zanspace.unloadedactivity;

import net.minecraft.util.RandomSource;

public record OccurrencesAndDuration (int occurrences, long duration) {
    public static OccurrencesAndDuration empty() {
        return new OccurrencesAndDuration(0, 0);
    }

    public static OccurrencesAndDuration recalculatedDuration(int occurrences, long cycles, double odds, RandomSource random) {
        return new OccurrencesAndDuration(occurrences, Utils.sampleNegativeBinomialWithMax(cycles, occurrences, odds, random));
    }
}
