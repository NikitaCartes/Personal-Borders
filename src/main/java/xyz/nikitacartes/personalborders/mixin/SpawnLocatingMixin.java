package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(SpawnLocating.class)
public class SpawnLocatingMixin{

    @ModifyExpressionValue(method = "locateSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private static WorldBorder modifyContains(WorldBorder original) {
        if (!(original instanceof EntityAdderImpl)) {
            return original;
        }
        Entity entity = ((EntityAdderImpl) original).personal_Borders$getEntity();
        BorderCache borderCache = getOfflineBorderCache(entity.getUuid());
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.getEntityWorld());
        }
        return original;
    }
}