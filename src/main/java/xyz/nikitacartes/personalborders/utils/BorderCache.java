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

public class BorderCache {

    private final WorldBorder overworldBorder;
    private final WorldBorder netherBorder;
    private final WorldBorder endBorder;

    public BorderCache(Node defaultBorderNote, Node overworldBorderNode, Node netherBorderNode, Node endBorderNode) {
        WorldBorder defaultBorder = getBorderFromNode(defaultBorderNote, PersonalBorders.server.getOverworld().getWorldBorder());

        this.overworldBorder = getBorderFromNode(overworldBorderNode, defaultBorder);
        this.netherBorder = getBorderFromNode(netherBorderNode, defaultBorder);
        this.endBorder = getBorderFromNode(endBorderNode, defaultBorder);
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
            // LogDebug("Sent overworld border to " + player.getNameForScoreboard());
            // LogDebug("{x: " + this.overworldBorder.getCenterX() + ", z: " + this.overworldBorder.getCenterZ() + ", radius: " + this.overworldBorder.getMaxRadius() + "}");
        } else if (player.getServerWorld().getRegistryKey().equals(World.NETHER)) {
            sendNetherBorder(player.networkHandler);
            // LogDebug("Sent nether border to " + player.getNameForScoreboard());
            // LogDebug("{x: " + this.netherBorder.getCenterX() + ", z: " + this.netherBorder.getCenterZ() + ", radius: " + this.netherBorder.getMaxRadius() + "}");
        } else if (player.getServerWorld().getRegistryKey().equals(World.END)) {
            sendEndBorder(player.networkHandler);
            // LogDebug("Sent end border to " + player.getNameForScoreboard());
            // LogDebug("{x: " + this.endBorder.getCenterX() + ", z: " + this.endBorder.getCenterZ() + ", radius: " + this.endBorder.getMaxRadius() + "}");
        }
    }

    private WorldBorder getBorderFromNode(Node node, WorldBorder defaultBorder) {
        if (node == null) {
            return defaultBorder;
        }

        Map<String, Set<String>> context = node.getContexts().toMap();
        int centerX = Integer.parseInt(context.getOrDefault("center.x", Set.of(Integer.toString((int)defaultBorder.getCenterX()))).iterator().next());
        int centerZ = Integer.parseInt(context.getOrDefault("center.z", Set.of(Integer.toString((int)defaultBorder.getCenterZ()))).iterator().next());
        int radius = Integer.parseInt(context.getOrDefault("radius", Set.of(Integer.toString(defaultBorder.getMaxRadius()))).iterator().next());
        int warningBlocks = Integer.parseInt(context.getOrDefault("warning.distance", Set.of(Integer.toString(defaultBorder.getWarningBlocks()))).iterator().next());
        int warningTime = Integer.parseInt(context.getOrDefault("warning.time", Set.of(Integer.toString(defaultBorder.getWarningTime()))).iterator().next());
        double damagePerBlock = Double.parseDouble(context.getOrDefault("damage.amount", Set.of(Double.toString(defaultBorder.getDamagePerBlock()))).iterator().next());
        double safeZone = Double.parseDouble(context.getOrDefault("damage.buffer", Set.of(Double.toString(defaultBorder.getSafeZone()))).iterator().next());

        WorldBorder border = new WorldBorder();
        border.setSize(radius * 2);
        border.setCenter(centerX, centerZ);
        border.setMaxRadius(radius);
        border.setWarningBlocks(warningBlocks);
        border.setWarningTime(warningTime);
        border.setDamagePerBlock(damagePerBlock);
        border.setSafeZone(safeZone);

        return border;
    }
}
