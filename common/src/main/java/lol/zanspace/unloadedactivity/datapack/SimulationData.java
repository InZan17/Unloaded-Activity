package lol.zanspace.unloadedactivity.datapack;

import java.util.*;

public class SimulationData {

    public Map<String, SimulateProperty> propertyMap;

    public SimulationData(IncompleteSimulationData incomplete) {
        HashMap<String, SimulateProperty> newPropertyMap = new HashMap<>();

        for (var entry : incomplete.propertyMap.entrySet()) {
            String key = entry.getKey();
            newPropertyMap.put(key, new SimulateProperty(entry.getValue(), key));
        }

        this.propertyMap = Map.copyOf(newPropertyMap);
    }

    public boolean isEmpty() {
        return this.propertyMap.isEmpty();
    }
}
