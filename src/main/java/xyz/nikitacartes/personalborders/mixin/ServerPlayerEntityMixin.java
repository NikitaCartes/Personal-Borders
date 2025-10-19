package xyz.nikitacartes.personalborders.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;


@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "getWorldSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/SpawnLocating;locateSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Ljava/util/concurrent/CompletableFuture;"))
    private void sendModifiedBorder(ServerWorld world, BlockPos basePos, CallbackInfoReturnable<BlockPos> cir) {
        ((EntityAdderImpl) world).personal_Borders$setEntity((ServerPlayerEntity) (Object) this);
    }
}