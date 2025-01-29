package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
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
            return borderCache.getWorldBorder(entity.getEntityWorld());
        }
        return defaultBorder;
    }

    @ModifyExpressionValue(method = "<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/world/ServerWorld;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/packet/c2s/common/SyncedClientOptions;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"))
    private static BlockPos sendModifiedBorder(BlockPos original, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) GameProfile profile) {
        BorderCache borderCache = getOfflineBorderCache(profile.getId());
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPos(world, border, original);
        }
        return original;
    }
}