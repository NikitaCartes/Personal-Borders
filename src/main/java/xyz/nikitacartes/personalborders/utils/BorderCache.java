package xyz.nikitacartes.personalborders.utils;

import net.luckperms.api.node.Node;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import xyz.nikitacartes.personalborders.PersonalBorders;

import java.util.Map;
import java.util.Set;

import static xyz.nikitacartes.personalborders.PersonalBorders.server;
import static xyz.nikitacartes.personalborders.utils.PersonalBordersLogger.LogDebug;

public class BorderCache {

    private final WorldBorder overworldBorder;
    private final WorldBorder netherBorder;
    private final WorldBorder endBorder;

    public BorderCache(Node defaultBorderNote, Node overworldBorderNode, Node netherBorderNode, Node endBorderNode) {
        WorldBorder defaultBorder = getBorderFromNode(defaultBorderNote, server.getOverworld().getWorldBorder(), 1);

        this.overworldBorder = getBorderFromNode(overworldBorderNode, defaultBorder, 1);
        this.netherBorder = getBorderFromNode(netherBorderNode, defaultBorder, 8);
        this.endBorder = getBorderFromNode(endBorderNode, defaultBorder,1);
    }

    public WorldBorder getOverworldBorder() {
        return this.overworldBorder;
    }

    public WorldBorder getNetherBorder() {
        return this.netherBorder;
    }

    public WorldBorder getEndBorder() {
        return this.endBorder;
    }

    public void sendOverworldBorder(ServerPlayNetworkHandler netHandler) {
        netHandler.sendPacket(new WorldBorderInitializeS2CPacket(this.overworldBorder));
    }

    public void sendNetherBorder(ServerPlayNetworkHandler netHandler) {
        netHandler.sendPacket(new WorldBorderInitializeS2CPacket(this.netherBorder));
    }

    public void sendEndBorder(ServerPlayNetworkHandler netHandler) {
        netHandler.sendPacket(new WorldBorderInitializeS2CPacket(this.endBorder));
    }

    public void sendBorder(ServerPlayerEntity player) {
        if (player.getServerWorld().getRegistryKey().equals(World.OVERWORLD)) {
            sendOverworldBorder(player.networkHandler);
            LogDebug("Sent overworld border to " + player.getNameForScoreboard());
            LogDebug("{x: " + this.overworldBorder.getCenterX() + ", z: " + this.overworldBorder.getCenterZ() + ", distance: " + this.overworldBorder.getSize() + "}");
        } else if (player.getServerWorld().getRegistryKey().equals(World.NETHER)) {
            sendNetherBorder(player.networkHandler);
            LogDebug("Sent nether border to " + player.getNameForScoreboard());
            LogDebug("{x: " + this.netherBorder.getCenterX() + ", z: " + this.netherBorder.getCenterZ() + ", distance: " + this.netherBorder.getSize() + "}");
        } else if (player.getServerWorld().getRegistryKey().equals(World.END)) {
            sendEndBorder(player.networkHandler);
            LogDebug("Sent end border to " + player.getNameForScoreboard());
            LogDebug("{x: " + this.endBorder.getCenterX() + ", z: " + this.endBorder.getCenterZ() + ", distance: " + this.endBorder.getSize() + "}");
        }
    }

    private WorldBorder getBorderFromNode(Node node, WorldBorder defaultBorder, double coordinateScale) {
        if (node == null) {
            return defaultBorder;
        }

        Map<String, Set<String>> context = node.getContexts().toMap();
        double centerX = Double.parseDouble(context.getOrDefault("center.x", Set.of(Double.toString(defaultBorder.getCenterX()))).iterator().next()) * coordinateScale;
        double centerZ = Double.parseDouble(context.getOrDefault("center.z", Set.of(Double.toString(defaultBorder.getCenterZ()))).iterator().next()) * coordinateScale;
        double distance = Double.parseDouble(context.getOrDefault("distance", Set.of(Double.toString(defaultBorder.getSize()))).iterator().next());
        int warningBlocks = Integer.parseInt(context.getOrDefault("warning.distance", Set.of(Integer.toString(defaultBorder.getWarningBlocks()))).iterator().next());
        int warningTime = Integer.parseInt(context.getOrDefault("warning.time", Set.of(Integer.toString(defaultBorder.getWarningTime()))).iterator().next());
        double damagePerBlock = Double.parseDouble(context.getOrDefault("damage.amount", Set.of(Double.toString(defaultBorder.getDamagePerBlock()))).iterator().next());
        double safeZone = Double.parseDouble(context.getOrDefault("damage.buffer", Set.of(Double.toString(defaultBorder.getSafeZone()))).iterator().next());

        WorldBorder border = new WorldBorder();
        border.setCenter(centerX, centerZ);
        border.setDamagePerBlock(damagePerBlock);
        border.setSafeZone(safeZone);
        border.setWarningBlocks(warningBlocks);
        border.setWarningTime(warningTime);
        border.setSize(distance);
        border.setMaxRadius(server.getMaxWorldBorderRadius());

        return border;
    }
}
