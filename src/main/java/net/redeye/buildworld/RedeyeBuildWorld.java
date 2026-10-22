package net.redeye.buildworld;

import net.neoforged.fml.common.Mod;

/**
 * redeye build world: World Switcher worlds created with {@link #FLAT_SEED} generate as a
 * Ponder-style flat (white / light grey concrete checkerboard, no structures, features or
 * mobs). Every other World Switcher world keeps the overworld's generator, so existing build
 * worlds (seed 20261021) are never changed.
 */
@Mod(RedeyeBuildWorld.MOD_ID)
public final class RedeyeBuildWorld {
    public static final String MOD_ID = "redeye_buildworld";
    /** "PONDER" on a phone keypad. */
    public static final long FLAT_SEED = 766337L;

    public RedeyeBuildWorld() {
    }
}
