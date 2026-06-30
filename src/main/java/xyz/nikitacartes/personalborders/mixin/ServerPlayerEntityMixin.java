package xyz.nikitacartes.personalborders.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;


@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/PlayerSpawnFinder;findSpawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Ljava/util/concurrent/CompletableFuture;"))
    private void sendModifiedBorder(ServerLevel world, BlockPos basePos, CallbackInfoReturnable<BlockPos> cir) {
        ((EntityAdderImpl) world).personal_Borders$setEntity((ServerPlayer) (Object) this);
    }
}
