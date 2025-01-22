package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;

@Mixin(Entity.class)
public class EntityMixin {

	@WrapOperation(method = "findCollisionsForMovement(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/World;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
	private static WorldBorder sendModifiedBorder(World instance, Operation<WorldBorder> original, Entity entity, World world) {
		if (entity != null && borders.containsKey(entity.getUuid())) {
			return borders.get(entity.getUuid()).getWorldBorder(world);
		}
		// Todo: add checks for other entities with owner
		return original.call(world);
	}
}