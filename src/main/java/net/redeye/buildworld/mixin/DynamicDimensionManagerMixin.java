package net.redeye.buildworld.mixin;

import net.geraldhofbauer.worldswitcher.world.DynamicDimensionManager;
import net.geraldhofbauer.worldswitcher.world.WorldRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.redeye.buildworld.CheckerboardFlatSource;
import net.redeye.buildworld.RedeyeBuildWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Swaps the overworld generator World Switcher uses for the checkerboard flat one, only for
 *  worlds created with {@link RedeyeBuildWorld#FLAT_SEED}. Runs on the server thread. */
@Mixin(value = DynamicDimensionManager.class, remap = false)
public abstract class DynamicDimensionManagerMixin {
    private static final ThreadLocal<WorldRegistry.WorldEntry> REDEYE_ENTRY = new ThreadLocal<>();
    private static final ThreadLocal<MinecraftServer> REDEYE_SERVER = new ThreadLocal<>();

    @Inject(method = "getOrCreateLevel", at = @At("HEAD"))
    private static void redeye$remember(MinecraftServer server, WorldRegistry.WorldEntry entry,
                                        CallbackInfoReturnable<ServerLevel> cir) {
        REDEYE_ENTRY.set(entry);
        REDEYE_SERVER.set(server);
    }

    @Redirect(method = "getOrCreateLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;getGenerator()Lnet/minecraft/world/level/chunk/ChunkGenerator;"))
    private static ChunkGenerator redeye$generator(ServerChunkCache overworldChunks) {
        WorldRegistry.WorldEntry entry = REDEYE_ENTRY.get();
        MinecraftServer server = REDEYE_SERVER.get();
        REDEYE_ENTRY.remove();
        REDEYE_SERVER.remove();
        if (entry != null && server != null && entry.seed() == RedeyeBuildWorld.FLAT_SEED) {
            return CheckerboardFlatSource.create(server);
        }
        return overworldChunks.getGenerator();
    }
}
