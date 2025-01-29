package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;
import static xyz.nikitacartes.personalborders.PersonalBorders.getModifiedSpawnPos;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

	@ModifyExpressionValue(method = "createTeleportTarget(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/TeleportTarget;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"))
	private BlockPos sendModifiedBorder(BlockPos original, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) Entity entity) {
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			WorldBorder border = borderCache.getWorldBorder(world);
			return getModifiedSpawnPos(world, border, original);
		}
		return original;
	}
}