package xyz.nikitacartes.personalborders.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.3 {
import net.minecraft.world.level.portal.TeleportTransition;
//?} else {
/*import net.minecraft.world.level.portal.DimensionTransition;
*///?}
//? if <1.21.9 {
/*import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.border.WorldBorder;
import xyz.nikitacartes.personalborders.utils.BorderCache;
*///?}
//? if <1.21.6 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
*///?}

import static xyz.nikitacartes.personalborders.PersonalBorders.*;


@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    // 1.21.9: the respawn search moved to PlayerSpawnFinder.findSpawn, which carries no entity,
    // so the player goes through pendingBorderCache. Older versions swap the receiver inline.
    //? if >=1.21.9 {
    @Inject(method = "adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"))
    private void sendModifiedBorder(ServerLevel world, BlockPos basePos, CallbackInfoReturnable<BlockPos> cir) {
        pendingBorderCache = getBorderCache((ServerPlayer) (Object) this);
    }
    //?} else {
    /*@ModifyReceiver(method = "adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getDistanceToBorder(DD)D"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, double x, double z) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        BorderCache borderCache = getOfflineBorderCache(entity.getUUID());
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }
    *///?}

    // 1.21.6: the login spawn read moved to PlayerList.placeNewPlayer (see PlayerManagerMixin).
    //? if <1.21.6 {
    /*@ModifyExpressionValue(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos sendModifiedSpawnPosition(BlockPos original, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) GameProfile profile) {
        BorderCache borderCache = getOfflineBorderCache(profile.getId());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
    *///?}

    // Read back by DismountingMixin.
    //? if >=1.21.3 {
    @Inject(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At("HEAD"))
    private void sendModifiedBorder(boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleport, CallbackInfoReturnable<TeleportTransition> cir) {
        pendingBorderCache = getBorderCache((ServerPlayer) (Object) this);
    }
    //?} else {
    /*@Inject(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;",
            at = @At("HEAD"))
    private void sendModifiedBorder(boolean consumeSpawnBlock, DimensionTransition.PostDimensionTransition postTeleport, CallbackInfoReturnable<DimensionTransition> cir) {
        pendingBorderCache = getBorderCache((ServerPlayer) (Object) this);
    }
    *///?}
}
