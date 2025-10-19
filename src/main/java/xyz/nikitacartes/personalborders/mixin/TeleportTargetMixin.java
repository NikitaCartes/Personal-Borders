package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(TeleportTarget.class)
public class TeleportTargetMixin {

    @ModifyExpressionValue(method = "getWorldSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPoint()Lnet/minecraft/world/WorldProperties$SpawnPoint;"))
    private static WorldProperties.SpawnPoint sendModifiedBorder(WorldProperties.SpawnPoint original, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "missingSpawnBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPoint()Lnet/minecraft/world/WorldProperties$SpawnPoint;"))
    private static WorldProperties.SpawnPoint sendModifiedBorder_noBlock(WorldProperties.SpawnPoint original, @Local(argsOnly = true) ServerPlayerEntity player) {
        BorderCache borderCache = getBorderCache(player);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(player.getEntityWorld());
            return getModifiedSpawnPoint(player.getEntityWorld(), border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "noRespawnPointSet(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPoint()Lnet/minecraft/world/WorldProperties$SpawnPoint;"))
    private static WorldProperties.SpawnPoint sendModifiedBorder_noPoint(WorldProperties.SpawnPoint original, @Local(argsOnly = true) ServerPlayerEntity player) {
        BorderCache borderCache = getBorderCache(player);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(player.getEntityWorld());
            return getModifiedSpawnPoint(player.getEntityWorld(), border, original);
        }
        return original;
    }
}