package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

	@ModifyReceiver(method = "canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
	private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos, @Local(argsOnly = true) PlayerEntity entity) {
		BorderCache borderCache = getBorderCache(entity);
		if (borderCache != null) {
			return borderCache.getWorldBorder(entity.getEntityWorld());
		}
		return defaultBorder;
	}
}