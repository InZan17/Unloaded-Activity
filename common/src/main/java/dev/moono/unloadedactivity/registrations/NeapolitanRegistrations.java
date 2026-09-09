package dev.moono.unloadedactivity.registrations;

import com.teamabnormals.neapolitan.core.Neapolitan;
import dev.moono.unloadedactivity.GameUtils;
import dev.moono.unloadedactivity.api.NumberFetcherRegistry;
import dev.moono.unloadedactivity.api.SimulationMethodRegistry;
import dev.moono.unloadedactivity.api.UnloadedActivityApi;
import dev.moono.unloadedactivity.impl.number_fetchers.neapolitan.IsWhiteValue;

public class NeapolitanRegistrations implements UnloadedActivityApi {
    @Override
    public void registerNumberFetchers(NumberFetcherRegistry registry) {
        registry.register(GameUtils.createId(Neapolitan.MOD_ID, "is_white"), new IsWhiteValue());
    }

    @Override
    public void registerSimulationMethods(SimulationMethodRegistry registry) {

    }
}
