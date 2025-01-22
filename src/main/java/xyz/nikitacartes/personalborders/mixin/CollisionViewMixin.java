package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;

@Mixin(CollisionView.class)
public interface CollisionViewMixin {

	@WrapOperation(method = "getWorldBorderCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Lnet/minecraft/util/shape/VoxelShape;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/CollisionView;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
	private WorldBorder sendModifiedBorder(CollisionView instance, Operation<WorldBorder> original, @Local(argsOnly = true) Entity entity) {
		if (entity != null && borders.containsKey(entity.getUuid())) {
			return borders.get(entity.getUuid()).getWorldBorder(entity.getEntityWorld());
		}
		// Todo: add checks for other entities with owner
		return original.call(instance);
	}

	@WrapOperation(method = "findClosestCollision(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/math/Vec3d;DDD)Ljava/util/Optional;",
			at = @At(value = "INVOKE",
					target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
	private Stream sendModifiedBorder(Stream instance, Predicate predicate, Operation<Stream> original, @Local(argsOnly = true) Entity entity) {
		if (entity != null && borders.containsKey(entity.getUuid())) {
			Predicate newPredicate = voxelShape -> borders.get(entity.getUuid())
					.getWorldBorder(entity.getEntityWorld())
					.contains(((VoxelShape)voxelShape).getBoundingBox());
			return original.call(instance, newPredicate);
		}
		return original.call(instance, predicate);
	}
}