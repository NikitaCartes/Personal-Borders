package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(TeleportTransition.class)
public class TeleportTargetMixin {

    @ModifyExpressionValue(method = "findAdjustedSharedSpawnPos(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private static LevelData.RespawnData sendModifiedBorder(LevelData.RespawnData original, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "missingRespawnBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private static LevelData.RespawnData sendModifiedBorder_noBlock(LevelData.RespawnData original, @Local(argsOnly = true) ServerPlayer player) {
        BorderCache borderCache = getBorderCache(player);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(player.level());
            return getModifiedSpawnPoint(player.level(), border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "createDefault(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private static LevelData.RespawnData sendModifiedBorder_noPoint(LevelData.RespawnData original, @Local(argsOnly = true) ServerPlayer player) {
        BorderCache borderCache = getBorderCache(player);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(player.level());
            return getModifiedSpawnPoint(player.level(), border, original);
        }
        return original;
    }
}
