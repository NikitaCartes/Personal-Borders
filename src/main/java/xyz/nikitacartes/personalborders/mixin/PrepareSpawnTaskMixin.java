package xyz.nikitacartes.personalborders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.network.config.PrepareSpawnTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.border.WorldBorder;
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
    NameAndId nameAndId;

    @Final
    @Shadow
    MinecraftServer server;

    @ModifyExpressionValue(method = "start",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/ServerLevelData;getRespawnData()Lnet/minecraft/world/level/storage/LevelData$RespawnData;"))
    private LevelData.RespawnData sendModifiedBorder(LevelData.RespawnData original) {
        BorderCache borderCache = getOfflineBorderCache(nameAndId.id());
        if (borderCache != null) {
            ServerLevel world = server.getLevel(original.globalPos().dimension());
            if (world == null) {
                world = server.overworld();
            }
            WorldBorder border = borderCache.getWorldBorder(world);
            return getModifiedSpawnPoint(world, border, original);
        }
        return original;
    }
}
