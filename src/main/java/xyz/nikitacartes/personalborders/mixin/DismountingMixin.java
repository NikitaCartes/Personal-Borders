package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.CollisionView;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;

@Mixin(Dismounting.class)
public class DismountingMixin {

	@ModifyReceiver(method = "canPlaceEntityAt(Lnet/minecraft/world/CollisionView;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/math/Box;)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/Box;)Z"))
	private static WorldBorder sendModifiedBorder(WorldBorder defaultBorder, Box box, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) CollisionView world) {
		if (entity != null && borders.containsKey(entity.getUuid())) {
			return borders.get(entity.getUuid()).getWorldBorder(entity.getEntityWorld());
		}
		// Todo: add checks for other entities with owner
		return defaultBorder;
	}
}