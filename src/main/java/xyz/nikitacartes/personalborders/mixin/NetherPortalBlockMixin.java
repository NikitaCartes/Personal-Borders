package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.EntityAdderImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;


@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

    @WrapOperation(method = "getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(ServerLevel world, Operation<WorldBorder> original, @Local(argsOnly = true) Entity entity) {
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(world);
        }
        return original.call(world);
    }

    @ModifyReceiver(method = "getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Lnet/minecraft/world/level/portal/TeleportTransition;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"))
    private PortalForcer sendModifiedBorder(PortalForcer portalForcer, BlockPos pos, Direction.Axis axis, @Local(argsOnly = true) Entity entity) {
        ((EntityAdderImpl) portalForcer).personal_Borders$setEntity(entity);
        return portalForcer;
    }
}
