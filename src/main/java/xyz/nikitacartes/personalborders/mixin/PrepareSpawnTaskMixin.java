package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.PrepareSpawnTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nikitacartes.personalborders.utils.BorderCache;

import static xyz.nikitacartes.personalborders.PersonalBorders.getModifiedSpawnPoint;
import static xyz.nikitacartes.personalborders.PersonalBorders.getOfflineBorderCache;


@Mixin(PrepareSpawnTask.class)
public class PrepareSpawnTaskMixin {

    @Final
    @Shadow
    PlayerConfigEntry player;

    @Final
    @Shadow
    MinecraftServer server;

    @ModifyExpressionValue(method = "sendPacket(Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerWorldProperties;getSpawnPoint()Lnet/minecraft/world/WorldProperties$SpawnPoint;"))
    private WorldProperties.SpawnPoint sendModifiedBorder(WorldProperties.SpawnPoint original) {
        BorderCache borderCache = getOfflineBorderCache(player.id());
        if (borderCache != null) {
            ServerWorld world = server.getWorld(original.getDimension());
            if (world == null) {
                world = server.getOverworld();
            }
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
}