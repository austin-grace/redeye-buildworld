package net.redeye.buildworld;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * redeye build world. A World Switcher world whose seed is one of the constants below is given
 * a flat generator (plains biome, no structures, lakes, features or starting mobs, floor at
 * y=62). Every other seed, including the existing build worlds on seed 20261021, keeps the
 * overworld generator World Switcher would have used.
 */
@Mod(RedeyeBuildWorld.MOD_ID)
public final class RedeyeBuildWorld {
    public static final String MOD_ID = "redeye_buildworld";
    /** Registry path of {@link PaintedFlatSource#CODEC}. */
    public static final String PAINTED_FLAT = "painted_flat";

    /** "PONDER" on a phone keypad. White / light grey 1-block checkerboard. */
    public static final long FLAT_SEED = 766337L;
    /** Grass, three dirt, stone. */
    public static final long PLAINS_SEED = 766338L;
    /** Smooth stone surface, stone below. */
    public static final long STONE_SEED = 766339L;
    /** White concrete with a light grey line on each chunk border. */
    public static final long PONDER_GRID_SEED = 766340L;

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<PaintedFlatSource>> PAINTED_FLAT_TYPE =
            CHUNK_GENERATORS.register(PAINTED_FLAT, () -> PaintedFlatSource.CODEC);

    public RedeyeBuildWorld(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }

    /**
     * The flat generator for {@code seed}, or null when this seed keeps normal World Switcher
     * generation. Null covers seed 20261021 and every seed that is not a preset.
     */
    public static ChunkGenerator generatorFor(HolderLookup.Provider registries, long seed) {
        if (seed == FLAT_SEED) {
            return PaintedFlatSource.create(registries, PaintedFlatSource.Preset.CHECKERBOARD);
        }
        if (seed == PLAINS_SEED) {
            return new FlatLevelSource(FlatPresets.plainsLayers(registries));
        }
        if (seed == STONE_SEED) {
            return new FlatLevelSource(FlatPresets.stoneLayers(registries));
        }
        if (seed == PONDER_GRID_SEED) {
            return PaintedFlatSource.create(registries, PaintedFlatSource.Preset.PONDER_GRID);
        }
        return null;
    }
}
