package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;
import static xyz.nikitacartes.personalborders.PersonalBorders.getModifiedSpawnPoint;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @ModifyExpressionValue(method = "getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private LevelData.RespawnData sendModifiedBorder(LevelData.RespawnData original, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
}
