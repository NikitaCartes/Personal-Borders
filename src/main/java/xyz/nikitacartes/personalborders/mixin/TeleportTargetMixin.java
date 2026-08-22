package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
//? if >=1.21.3 {
import net.minecraft.world.level.portal.TeleportTransition;
//?} else {
/*import net.minecraft.world.level.portal.DimensionTransition;
*///?}
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

// 1.21.2: DimensionTransition became TeleportTransition.
//? if >=1.21.3 {
@Mixin(TeleportTransition.class)
//?} else {
/*@Mixin(DimensionTransition.class)
*///?}
public class TeleportTargetMixin {

    // 1.21.9: the spawn BlockPos became RespawnData, plus two fallbacks for a missing respawn block.
    //? if >=1.21.9 {
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
    //?} else {
    /*@ModifyExpressionValue(method = "findAdjustedSharedSpawnPos(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sendModifiedBorder(BlockPos original, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
    *///?}
}
