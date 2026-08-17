package xyz.nikitacartes.personalborders.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;


@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    // Read back by SpawnLocatingMixin, inside PlayerSpawnFinder.findSpawn.
    @Inject(method = "adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"))
    private void sendModifiedBorder(ServerLevel world, BlockPos basePos, CallbackInfoReturnable<BlockPos> cir) {
        pendingBorderCache = getBorderCache((ServerPlayer) (Object) this);
    }

    // Read back by DismountingMixin, inside DismountHelper.findSafeDismountLocation.
    @Inject(method = "findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At("HEAD"))
    private void sendModifiedBorder(boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleport, CallbackInfoReturnable<TeleportTransition> cir) {
        pendingBorderCache = getBorderCache((ServerPlayer) (Object) this);
    }
}
