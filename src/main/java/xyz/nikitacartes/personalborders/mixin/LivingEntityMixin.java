package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@ModifyReceiver(method = "baseTick()V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/Box;)Z"))
	private WorldBorder modifyContains(WorldBorder defaultBorder, Box box) {
		LivingEntity entity = ((LivingEntity)(Object)this);
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(entity.getEntityWorld());
		}
		return defaultBorder;
	}

	@ModifyReceiver(method = "baseTick()V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;getDistanceInsideBorder(Lnet/minecraft/entity/Entity;)D"))
	private WorldBorder modifyDistanceInsideBorder(WorldBorder defaultBorder, Entity entity) {
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(entity.getEntityWorld());
		}
		return defaultBorder;
	}

	@ModifyReceiver(method = "baseTick()V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;getSafeZone()D"))
	private WorldBorder modifySafeZone(WorldBorder defaultBorder) {
		LivingEntity entity = ((LivingEntity)(Object)this);
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(entity.getEntityWorld());
		}
		return defaultBorder;
	}

	@ModifyReceiver(method = "baseTick()V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;getDamagePerBlock()D"))
	private WorldBorder modifyDamagePerBlock(WorldBorder defaultBorder) {
		LivingEntity entity = ((LivingEntity)(Object)this);
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(entity.getEntityWorld());
		}
		return defaultBorder;
	}
}