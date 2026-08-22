package xyz.nikitacartes.personalborders.mixin;

// PlayerSpawnFinder exists from 1.21.9 on.
//? if >=1.21.9 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(PlayerSpawnFinder.class)
public class SpawnLocatingMixin{

    // Static, so the player comes from ServerPlayerEntityMixin (respawn) or PrepareSpawnTaskMixin (login).
    @ModifyExpressionValue(method = "findSpawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder modifyContains(WorldBorder original, @Local(argsOnly = true) ServerLevel world) {
        if (pendingBorderCache != null) {
            return pendingBorderCache.getWorldBorder(world);
        }
        return original;
    }
}
//?}
