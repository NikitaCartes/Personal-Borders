package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.PortalForcer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.imlp.PortalForcerImpl;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;

@Mixin(PortalForcer.class)
public class PortalForcerMixin implements PortalForcerImpl {

	@Unique
	@Nullable Entity entity;

	@WrapOperation(method = "createPortal(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction$Axis;)Ljava/util/Optional;",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
	private WorldBorder sendModifiedBorder(ServerWorld instance, Operation<WorldBorder> original) {
		if (entity != null && borders.containsKey(entity.getUuid())) {
			return borders.get(entity.getUuid()).getWorldBorder(entity.getEntityWorld());
		}
		// Todo: add checks for other entities with owner
		return original.call(instance);
	}

	@Override
	public void personal_Borders$setEntity(@Nullable Entity entity) {
		this.entity = entity;
	}

	@Override
	public @Nullable Entity personal_Borders$getEntity() {
		return this.entity;
	}
}