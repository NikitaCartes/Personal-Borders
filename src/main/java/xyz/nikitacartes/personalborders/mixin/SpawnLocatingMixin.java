package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(PlayerSpawnFinder.class)
public class SpawnLocatingMixin{

    @ModifyExpressionValue(method = "findSpawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private static WorldBorder modifyContains(WorldBorder original) {
        if (!(original instanceof EntityAdderImpl)) {
            return original;
        }
        Entity entity = ((EntityAdderImpl) original).personal_Borders$getEntity();
        BorderCache borderCache = getOfflineBorderCache(entity.getUUID());
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return original;
    }
}
