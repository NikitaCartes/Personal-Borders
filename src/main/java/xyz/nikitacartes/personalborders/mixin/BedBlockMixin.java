package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    @ModifyReceiver(method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private WorldBorder sendModifiedBorder(WorldBorder defaultBorder, BlockPos pos, BlockPlaceContext ctx) {
        BorderCache borderCache = getBorderCache(ctx.getPlayer());
        if (borderCache != null) {
            return borderCache.getWorldBorder(ctx.getLevel());
        }
        return defaultBorder;
    }
}
