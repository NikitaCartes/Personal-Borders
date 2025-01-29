package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.borders;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @ModifyExpressionValue(method = "sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private WorldBorder sendModifiedBorder(WorldBorder original, ServerPlayerEntity player, ServerWorld world) {
        if (player == null || player.getUuid() == null) {
            return original;
        }
        BorderCache border = borders.get(player.getUuid());
        if (border == null) {
            return original;
        }
        if (world.getRegistryKey().equals(ServerWorld.OVERWORLD)) {
            LogDebug("Sent overworld border to " + player.getNameForScoreboard());
            LogDebug("{x: " + border.getOverworldBorder().getCenterX() + ", z: " + border.getOverworldBorder().getCenterZ() + ", distance: " + border.getOverworldBorder().getSize() + "}");
            return border.getOverworldBorder();
        } else if (world.getRegistryKey().equals(ServerWorld.NETHER)) {
            LogDebug("Sent nether border to " + player.getNameForScoreboard());
            LogDebug("{x: " + border.getNetherBorder().getCenterX() + ", z: " + border.getNetherBorder().getCenterZ() + ", distance: " + border.getNetherBorder().getSize() + "}");
            return border.getNetherBorder();
        } else if (world.getRegistryKey().equals(ServerWorld.END)) {
            LogDebug("Sent end border to " + player.getNameForScoreboard());
            LogDebug("{x: " + border.getEndBorder().getCenterX() + ", z: " + border.getEndBorder().getCenterZ() + ", distance: " + border.getEndBorder().getSize() + "}");
            return border.getEndBorder();
        }
        return original;
    }

    @WrapOperation(method = "setMainWorld(Lnet/minecraft/server/world/ServerWorld;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;addListener(Lnet/minecraft/world/border/WorldBorderListener;)V"))
    private void setMainWorld(WorldBorder instance, WorldBorderListener listener, Operation<Void> original) {
        // do nothing and don't call original
    }
}