package lol.zanspace.unloadedactivity.datapack;

import java.util.*;

public class SimulationData {

    public Map<String, SimulateProperty> propertyMap;

    public SimulationData(IncompleteSimulationData incomplete) {
        HashMap<String, SimulateProperty> newPropertyMap = new HashMap<>();

        for (var entry : incomplete.propertyMap.entrySet()) {
            String key = entry.getKey();
            try {
                newPropertyMap.put(key, new SimulateProperty(entry.getValue(), key));
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify property " + key + ".\n" + e.getMessage());
            }
        }

        this.propertyMap = Map.copyOf(newPropertyMap);
    }

    public boolean isEmpty() {
        return this.propertyMap.isEmpty();
    }
}
