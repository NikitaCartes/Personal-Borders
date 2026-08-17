package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // Read back by DismountingMixin, inside DismountHelper.findSafeDismountLocation.
    @Inject(method = "stopSleeping()V", at = @At("HEAD"))
    private void sendModifiedBorder(CallbackInfo ci) {
        pendingBorderCache = getBorderCache((LivingEntity) (Object) this);
    }

    @ModifyReceiver(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, AABB box) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    @ModifyReceiver(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getDistanceToBorder(Lnet/minecraft/world/entity/Entity;)D"))
    private WorldBorder modifyDistanceInsideBorder(WorldBorder defaultBorder, Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    @ModifyReceiver(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getSafeZone()D"))
    private WorldBorder modifySafeZone(WorldBorder defaultBorder) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    @ModifyReceiver(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;getDamagePerBlock()D"))
    private WorldBorder modifyDamagePerBlock(WorldBorder defaultBorder) {
        LivingEntity entity = ((LivingEntity)(Object)this);
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }
}
