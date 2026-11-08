package net.redeye.buildworld;

import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Flat generator whose top concrete layer is painted from a preset. The preset is part of
 * {@link #CODEC}, registered as {@code redeye_buildworld:painted_flat}, so a saved generator
 * reads back as the same pattern after a restart.
 */
public class PaintedFlatSource extends FlatLevelSource {
    private static final BlockState DARK = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();

    public enum Preset implements StringRepresentable {
        /** 1-block white / light grey checkerboard. Seed {@link RedeyeBuildWorld#FLAT_SEED}. */
        CHECKERBOARD("checkerboard"),
        /** White floor with a light grey line on every chunk border. Seed {@link RedeyeBuildWorld#PONDER_GRID_SEED}. */
        PONDER_GRID("ponder_grid");

        public static final Codec<Preset> CODEC = StringRepresentable.fromEnum(Preset::values);

        private final String name;

        Preset(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final MapCodec<PaintedFlatSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(PaintedFlatSource::settings),
            Preset.CODEC.fieldOf("preset").forGetter(PaintedFlatSource::preset)
    ).apply(instance, PaintedFlatSource::new));

    private final Preset preset;
    private final int topLayer;

    public PaintedFlatSource(FlatLevelGeneratorSettings settings, Preset preset) {
        super(settings);
        this.preset = preset;
        this.topLayer = settings.getLayers().size() - 1;
    }

    public static PaintedFlatSource create(HolderLookup.Provider registries, Preset preset) {
        return new PaintedFlatSource(FlatPresets.concreteFloor(registries), preset);
    }

    public Preset preset() {
        return this.preset;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /**
     * Whether the top block at this column is light grey concrete. Checkerboard parity matches
     * the 1.0.0 generator, including negative coordinates. The grid uses {@link Math#floorMod}
     * so a negative chunk border ({@code x % 16 == 0} in floor arithmetic) is still a line.
     */
    static boolean paintsDark(Preset preset, int x, int z) {
        return switch (preset) {
            case CHECKERBOARD -> ((x + z) & 1) != 0;
            case PONDER_GRID -> Math.floorMod(x, 16) == 0 || Math.floorMod(z, 16) == 0;
        };
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
                    int worldX = baseX + x;
                    int worldZ = baseZ + z;
                    if (paintsDark(preset, worldX, worldZ)) {
                        filled.setBlockState(pos.set(worldX, y, worldZ), DARK, false);
                    }
                }
            }
            return filled;
        });
    }
}
