package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.BorderChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @ModifyExpressionValue(method = "sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(WorldBorder original, ServerPlayer player, ServerLevel world) {
        if (player == null || player.getUUID() == null) {
            return original;
        }
        BorderCache border = borders.get(player.getUUID());
        if (border == null) {
            return original;
        }
        if (world.dimension().equals(Level.OVERWORLD)) {
            LogDebug("Sent overworld border to " + player.getScoreboardName());
            LogDebug("{x: " + border.getOverworldBorder().getCenterX() + ", z: " + border.getOverworldBorder().getCenterZ() + ", distance: " + border.getOverworldBorder().getSize() + "}");
            return border.getOverworldBorder();
        } else if (world.dimension().equals(Level.NETHER)) {
            LogDebug("Sent nether border to " + player.getScoreboardName());
            LogDebug("{x: " + border.getNetherBorder().getCenterX() + ", z: " + border.getNetherBorder().getCenterZ() + ", distance: " + border.getNetherBorder().getSize() + "}");
            return border.getNetherBorder();
        } else if (world.dimension().equals(Level.END)) {
            LogDebug("Sent end border to " + player.getScoreboardName());
            LogDebug("{x: " + border.getEndBorder().getCenterX() + ", z: " + border.getEndBorder().getCenterZ() + ", distance: " + border.getEndBorder().getSize() + "}");
            return border.getEndBorder();
        }
        return original;
    }

    @WrapOperation(method = "addWorldborderListener(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"))
    private void setMainWorld(WorldBorder instance, BorderChangeListener listener, Operation<Void> original) {
        // Never registered: borders are per-player.
    }

    // 1.21.9: the spawn BlockPos became RespawnData.
    //? if >=1.21.9 {
    @ModifyExpressionValue(method = "sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private static LevelData.RespawnData sendModifiedWorldInfo(LevelData.RespawnData original, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) ServerLevel world) {
        BorderCache borderCache = getOfflineBorderCache(player.getUUID());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private static LevelData.RespawnData sendModifiedRespawnPosition(LevelData.RespawnData original, @Local(argsOnly = true) ServerPlayer player, @Local(ordinal = 0) ServerLevel serverWorld) {
        BorderCache borderCache = getOfflineBorderCache(player.getUUID());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(serverWorld);
            return getModifiedSpawnPoint(serverWorld, border, original);
        }
        return original;
    }
    //?} else {
    /*@ModifyExpressionValue(method = "sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sendModifiedWorldInfo(BlockPos original, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) ServerLevel world) {
        BorderCache borderCache = getOfflineBorderCache(player.getUUID());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sendModifiedRespawnPosition(BlockPos original, @Local(argsOnly = true) ServerPlayer player, @Local(ordinal = 0) ServerLevel serverWorld) {
        BorderCache borderCache = getOfflineBorderCache(player.getUUID());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(serverWorld);
            return getModifiedSpawnPoint(serverWorld, border, original);
        }
        return original;
    }
    *///?}

    // The spawn is read here only in 1.21.6 - 1.21.8: before that in the ServerPlayer
    // constructor, after that in PrepareSpawnTask.
    //? if >=1.21.6 <1.21.9 {
    /*@ModifyExpressionValue(method = "placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sendModifiedSpawnPosition(BlockPos original, @Local(argsOnly = true) ServerPlayer player) {
        BorderCache borderCache = getOfflineBorderCache(player.getUUID());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(player.level());
            return getModifiedSpawnPoint(player.level(), border, original);
        }
        return original;
    }
    *///?}
}
