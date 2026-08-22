package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(CollisionGetter.class)
public interface CollisionViewMixin {

    @WrapOperation(method = "borderCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/CollisionGetter;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(CollisionGetter instance, Operation<WorldBorder> original, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return original.call(instance);
    }

    // clipIncludingBorder exists from 1.21.2 on.
    //? if >=1.21.3 {
    @WrapOperation(method = "clipIncludingBorder(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/CollisionGetter;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(CollisionGetter instance, Operation<WorldBorder> original, @Local(argsOnly = true) ClipContext context) {
        Entity entity = ((EntityCollisionContext) context.collisionContext).getEntity();
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null && entity != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return original.call(instance);
    }
    //?}

    @WrapOperation(method = "findFreePosition(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/Vec3;DDD)Ljava/util/Optional;",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Stream sendModifiedBorder(Stream instance, Predicate predicate, Operation<Stream> original, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            Predicate newPredicate = voxelShape -> borderCache
                    .getWorldBorder(entity.level())
                    .isWithinBounds(((VoxelShape)voxelShape).bounds());
            return original.call(instance, newPredicate);
        }
        return original.call(instance, predicate);
    }
}
