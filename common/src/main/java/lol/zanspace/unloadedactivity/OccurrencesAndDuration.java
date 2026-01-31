package lol.zanspace.unloadedactivity;

public record OccurrencesAndDuration (int occurrences, long duration) {
    public static OccurrencesAndDuration empty() {
        return new OccurrencesAndDuration(0, 0);
    }
}
