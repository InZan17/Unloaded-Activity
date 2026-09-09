package dev.moono.unloadedactivity.impl.number_fetchers;

import dev.moono.unloadedactivity.api.context.WeatherDependantContext;
import dev.moono.unloadedactivity.api.number_fetcher.WeatherDependantNumberFetcher;

public class IsRainingValue implements WeatherDependantNumberFetcher {
    @Override
    public Number evaluate(WeatherDependantContext context) {
        return context.isRaining() ? 1 : 0;
    }
}
