package net.redeye.buildworld;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * Layer stacks for the flat World Switcher seeds. World Switcher copies the overworld
 * dimension type, so the stack starts at y=-64. 127 layers put the top of the floor at y=62,
 * the same height as the original checkerboard. Every preset is bedrock, then stone, then a
 * thin surface, with plains, no structure sets, and lakes and features left off.
 */
final class FlatPresets {
    static final int MIN_BUILD_Y = -64;
    static final int FLOOR_Y = 62;
    static final int LAYER_COUNT = FLOOR_Y - MIN_BUILD_Y + 1;

    private FlatPresets() {
    }

    static Holder<Biome> plains(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
    }

    /** Grass block, three dirt, stone, bedrock. Vanilla {@link FlatLevelSource} settings. */
    static FlatLevelGeneratorSettings plainsLayers(HolderLookup.Provider registries) {
        return stack(plains(registries), List.of(
                new FlatLayerInfo(1, Blocks.BEDROCK),
                new FlatLayerInfo(122, Blocks.STONE),
                new FlatLayerInfo(3, Blocks.DIRT),
                new FlatLayerInfo(1, Blocks.GRASS_BLOCK)));
    }

    /** Smooth stone surface, stone, bedrock. Vanilla {@link FlatLevelSource} settings. */
    static FlatLevelGeneratorSettings stoneLayers(HolderLookup.Provider registries) {
        return stack(plains(registries), List.of(
                new FlatLayerInfo(1, Blocks.BEDROCK),
                new FlatLayerInfo(125, Blocks.STONE),
                new FlatLayerInfo(1, Blocks.SMOOTH_STONE)));
    }

    /** White concrete surface, stone, bedrock. The painted generator recolors the top layer. */
    static FlatLevelGeneratorSettings concreteFloor(HolderLookup.Provider registries) {
        return stack(plains(registries), List.of(
                new FlatLayerInfo(1, Blocks.BEDROCK),
                new FlatLayerInfo(125, Blocks.STONE),
                new FlatLayerInfo(1, Blocks.WHITE_CONCRETE)));
    }

    private static FlatLevelGeneratorSettings stack(Holder<Biome> plains, List<FlatLayerInfo> layers) {
        int height = 0;
        for (FlatLayerInfo layer : layers) {
            height += layer.getHeight();
        }
        if (height != LAYER_COUNT) {
            throw new IllegalArgumentException(
                    "flat preset must be " + LAYER_COUNT + " layers so the floor is y=" + FLOOR_Y + ", was " + height);
        }
        Optional<HolderSet<StructureSet>> noStructures = Optional.of(HolderSet.direct());
        return new FlatLevelGeneratorSettings(noStructures, plains, List.of())
                .withBiomeAndLayers(layers, noStructures, plains);
    }
}
