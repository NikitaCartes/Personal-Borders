package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @ModifyReceiver(method = "getWorldSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;getDistanceInsideBorder(DD)D"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, double x, double z) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        BorderCache borderCache = getOfflineBorderCache(entity.getUuid());
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.getWorld());
        }
        return defaultBorder;
    }
}