package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements EntityAdderImpl {

    @Unique
    @Nullable Entity addedEntity;

    @ModifyReceiver(method = "canEntityModifyAt(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.getEntityWorld());
        }
        return defaultBorder;
    }

    @ModifyReturnValue(method = "canEntityModifyAt(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At("RETURN"))
    private boolean modifyReturnValue(boolean original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockPos pos) {
        if (original && !(entity instanceof PlayerEntity)) {
            BorderCache borderCache = getBorderCache(entity);
            if (borderCache != null) {
                return borderCache.getWorldBorder(entity.getEntityWorld()) .contains(pos);
            }
        }
        return original;
    }

    @Override
    public void personal_Borders$setEntity(@Nullable Entity entity) {
        this.addedEntity = entity;
    }

    @Override
    public @Nullable Entity personal_Borders$getEntity() {
        return this.addedEntity;
    }
}