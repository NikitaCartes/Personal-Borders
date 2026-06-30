package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getBorderCache;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {

    @ModifyReceiver(method = "handleInteract(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private WorldBorder modifyContains(WorldBorder defaultBorder, BlockPos pos) {
        LivingEntity entity = ((ServerGamePacketListenerImpl)(Object)this).player;
        BorderCache borderCache = getBorderCache(entity);
        if (borderCache != null) {
            return borderCache.getWorldBorder(entity.level());
        }
        return defaultBorder;
    }
}
