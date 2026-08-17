package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(DismountHelper.class)
public class DismountingMixin {

    @ModifyReceiver(method = "canDismountTo(Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z"))
    private static WorldBorder sendModifiedBorder(WorldBorder defaultBorder, AABB box, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) CollisionGetter world) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    // Stand-up search after leaving a bed or a respawn anchor. The method carries an entity type, not the
    // entity, so the border comes from the player that started the search.
    @ModifyReceiver(method = "findSafeDismountLocation(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/phys/Vec3;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z"))
    private static WorldBorder sendPendingBorder(WorldBorder defaultBorder, AABB box, @Local(argsOnly = true) CollisionGetter world) {
        if (pendingBorderCache != null && world instanceof Level level) {
            return pendingBorderCache.getWorldBorder(level);
        }
        return defaultBorder;
    }
}
