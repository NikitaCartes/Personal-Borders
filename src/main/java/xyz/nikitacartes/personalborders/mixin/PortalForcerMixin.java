package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(PortalForcer.class)
public class PortalForcerMixin implements EntityAdderImpl {

    @Unique
    @Nullable Entity entity;

    @WrapOperation(method = "createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(ServerLevel instance, Operation<WorldBorder> original) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null && entity != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return original.call(instance);
    }

    @Override
    public void personal_Borders$setEntity(@Nullable Entity entity) {
        this.entity = entity;
    }
}
