package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(Entity.class)
public class EntityMixin {

    // 26.2 moved the movement-collision border lookup out of collectColliders into collectCollidersIgnoringWorldBorder.
    //? if <26.2 {
    @WrapOperation(method = "collectColliders(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    //?} else {
    /*@WrapOperation(method = "collectCollidersIgnoringWorldBorder(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    *///?}
    private static WorldBorder sendModifiedBorder(Level instance, Operation<WorldBorder> original, Entity entity, Level world) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(world);
        }
        return original.call(world);
    }

    @ModifyExpressionValue(method = "adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private LevelData.RespawnData sendModifiedBorder(LevelData.RespawnData original, @Local(argsOnly = true) ServerLevel world) {
        Entity entity = ((Entity)(Object)this);
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
}
