package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(ServerLevel.class)
public class ServerWorldMixin implements EntityAdderImpl {

    @Unique
    @Nullable Entity addedEntity;

    @ModifyReceiver(method = "mayInteract(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }

    @ModifyReturnValue(method = "mayInteract(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("RETURN"))
    private boolean modifyReturnValue(boolean original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) BlockPos pos) {
        if (original && !(entity instanceof Player)) {
            BorderCache borderCache = getBorderCache(entity);
            if (borderCache != null) {
                return borderCache.getWorldBorder(entity.level()) .isWithinBounds(pos);
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
