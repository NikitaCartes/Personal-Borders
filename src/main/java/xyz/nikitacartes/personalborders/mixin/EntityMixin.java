package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(Entity.class)
public class EntityMixin {

    @WrapOperation(method = "findCollisionsForMovement(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private static WorldBorder sendModifiedBorder(World instance, Operation<WorldBorder> original, Entity entity, World world) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(world);
        }
        return original.call(world);
    }

    @ModifyExpressionValue(method = "getWorldSpawnPos(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos sendModifiedBorder(BlockPos original, @Local(argsOnly = true) ServerWorld world) {
        Entity entity = ((Entity)(Object)this);
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPos(world, border, original);
        }
        return original;
    }
}