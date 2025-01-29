package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.block.BedBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.*;

@Mixin(BedBlock.class)
public class BedBlockMixin {

    @ModifyReceiver(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
    private WorldBorder sendModifiedBorder(WorldBorder defaultBorder, BlockPos pos, ItemPlacementContext ctx) {
        BorderCache borderCache = getBorderCache(ctx.getPlayer());
        if (borderCache != null) {
            return borderCache.getWorldBorder(ctx.getWorld());
        }
        return defaultBorder;
    }
}