package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    // 1.21.5: mayInteract widened from Player to Entity. The return-value check stops
    // non-players reaching outside their owner's border.
    //? if >=1.21.5 {
    @ModifyReceiver(method = "mayInteract(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    @ModifyReturnValue(method = "mayInteract(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("RETURN"))
    private boolean modifyReturnValue(boolean original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockPos pos) {
        if (original && !(entity instanceof Player)) {
            BorderCache borderCache = getBorderCache(entity);
            if (borderCache != null) {
                return borderCache.getWorldBorder(entity.level()) .isWithinBounds(pos);
            }
        }
        return original;
    }
    //?} else {
    /*@ModifyReceiver(method = "mayInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos, @Local(argsOnly = true) Player entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }
    *///?}
}
