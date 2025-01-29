package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.PortalForcerImpl;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;


@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

	@WrapOperation(method = "createTeleportTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
	private WorldBorder sendModifiedBorder(ServerWorld world, Operation<WorldBorder> original, @Local(argsOnly = true) Entity entity) {
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(world);
		}
		return original.call(world);
	}

	@ModifyReceiver(method = "getOrCreateExitPortalTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/world/border/WorldBorder;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/dimension/PortalForcer;createPortal(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction$Axis;)Ljava/util/Optional;"))
	private PortalForcer sendModifiedBorder(PortalForcer portalForcer, BlockPos pos, Direction.Axis axis, @Local(argsOnly = true) Entity entity) {
		((PortalForcerImpl) portalForcer).personal_Borders$setEntity(entity);
		return portalForcer;
	}
}