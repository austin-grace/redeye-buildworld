package net.redeye.buildworld;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * A vanilla flat generator (bedrock, stone, a concrete top at y 62; plains biome; no structures,
 * features, lakes or starting mobs) whose top layer is painted as a 1-block checkerboard of
 * white and light grey concrete, like Create's Ponder scenes. It reuses the flat codec, so
 * nothing new is written to save files.
 */
public class CheckerboardFlatSource extends FlatLevelSource {
    private static final BlockState LIGHT = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState DARK = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();

    private final int topLayer;

    public CheckerboardFlatSource(FlatLevelGeneratorSettings settings) {
        super(settings);
        this.topLayer = settings.getLayers().size() - 1;
    }

    public static CheckerboardFlatSource create(MinecraftServer server) {
        var plains = server.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        Optional<HolderSet<net.minecraft.world.level.levelgen.structure.StructureSet>> noStructures =
                Optional.of(HolderSet.direct());
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(noStructures, plains, List.of())
                .withBiomeAndLayers(List.of(
                        new FlatLayerInfo(1, Blocks.BEDROCK),
                        new FlatLayerInfo(125, Blocks.STONE),   // floor at y 62, as high as sea level, so the horizon stays light
                        new FlatLayerInfo(1, Blocks.WHITE_CONCRETE)), noStructures, plains);
        return new CheckerboardFlatSource(settings);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        return super.fillFromNoise(blender, randomState, structureManager, chunk).thenApply(filled -> {
            int y = filled.getMinBuildHeight() + topLayer;
            int baseX = filled.getPos().getMinBlockX();
            int baseZ = filled.getPos().getMinBlockZ();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (((baseX + x + baseZ + z) & 1) != 0) {
                        filled.setBlockState(pos.set(baseX + x, y, baseZ + z), DARK, false);
                    }
                }
            }
            return filled;
        });
    }
}
