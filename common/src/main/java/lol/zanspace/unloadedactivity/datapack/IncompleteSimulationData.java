package lol.zanspace.unloadedactivity.datapack;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import lol.zanspace.unloadedactivity.UnloadedActivity;
import lol.zanspace.unloadedactivity.Utils;
import lol.zanspace.unloadedactivity.datapack.condition.StaticCondition;
import lol.zanspace.unloadedactivity.mixin.CropBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IncompleteSimulationData {
    public static final Codec<IncompleteSimulationData> CODEC;

    public Map<String, IncompleteSimulateProperty> propertyMap;

    public IncompleteSimulationData() {
        this.propertyMap = new HashMap<>();
    }

    public boolean isEmpty() {
        return this.propertyMap.isEmpty();
    }

    public void merge(IncompleteSimulationData otherSimulationData) {
        for (var entry : otherSimulationData.propertyMap.entrySet()) {
            var thisSimulateProperty = this.propertyMap.computeIfAbsent(entry.getKey(), k -> new IncompleteSimulateProperty());
            var otherSimulateProperty = entry.getValue();

            thisSimulateProperty.merge(otherSimulateProperty);
        }
    }

    static {
        CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(IncompleteSimulationData input, DynamicOps<T> ops, T prefix) {
                throw new UnsupportedOperationException("I am never using this. Therefore, it does not need to be implemented.");
            }

            @Override
            public <T> DataResult<Pair<IncompleteSimulationData, T>> decode(DynamicOps<T> ops, T input) {
                IncompleteSimulationData simulationData = new IncompleteSimulationData();

                var mapResult = ops.getMap(input);

                if (mapResult.error().isPresent()) {
                    return returnError(mapResult);
                }
                MapLike<T> map = mapResult.result().get();

                for (var pair : map.entries().toList()) {
                    T key = pair.getFirst();
                    T propertyInfo = pair.getSecond();

                    var propertyNameResult = ops.getStringValue(key);

                    if (!propertyNameResult.result().isPresent()) {
                        return returnError(propertyNameResult);
                    }

                    String propertyName = propertyNameResult.result().get();

                    DataResult<IncompleteSimulateProperty> simulateProperty = IncompleteSimulateProperty.parse(ops, propertyInfo);

                    if (!simulateProperty.result().isPresent()) {
                        return returnError(simulateProperty);
                    }

                    simulationData.propertyMap.put(propertyName, simulateProperty.result().get());
                }
                return DataResult.success(Pair.of(simulationData, ops.empty()));
            }
        };
    }

    static <R> DataResult<R> returnError(DataResult<?> dataResult) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> dataResult.error().get().message());
        #else
        return DataResult.error(dataResult.error().get().message());
        #endif
    }

    static <R> DataResult<R> returnError(String info, DataResult<?> dataResult) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> info + dataResult.error().get().message());
        #else
        return DataResult.error(info + "\n" + dataResult.error().get().message());
        #endif
    }

    static <R> DataResult<R> returnError(String info) {
        #if MC_VER >= MC_1_19_4
        return DataResult.error(() -> info);
        #else
        return DataResult.error(info);
        #endif
    }
}
